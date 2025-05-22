package org.tests;

import data.TestDataSeeder;
import io.qameta.allure.*;
import io.restassured.response.Response;
import mainutils.ProductCleanupService;
import mainutils.UserCleanupService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import client.ProductClient;
import domain.model.Product;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import testutil.TestUtils;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("Тестирование уровня QA Level 1 – Базовые проверки")
@Feature("Проверка идентификаторов, цен и базовых ограничений")
public class QaLevel1Test extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(QaLevel1Test.class);

    private final ProductClient productClient = new ProductClient();
    private final TestDataSeeder seeder = new TestDataSeeder();
    private List<Long> productIds;

    @BeforeClass
    public void setup() {
//        logger.info("📦 Очистка ранее созданных данных");
//        UserCleanupService cleanerUser = new UserCleanupService();
//        ProductCleanupService cleanerProd = new ProductCleanupService();
//        cleanerUser.cleanUpAllUsers();    // Полностью очищает пользователей в системе.
//        cleanerProd.cleanUpAllProducts(); // Полная очистка с понижением цены

//        logger.info("Загрузка мок-данных перед тестами уровня 1");
//        seeder.seedAll();
//        productIds = seeder.getCreatedProductIds();

            List<Long> existingIds = productClient.getAllProductIds(); // достаём ID из сервера

            if (existingIds.isEmpty()) {
                System.out.println("Продуктов нет. Сидируем данные...");
                seeder.seedAll();
                productIds = seeder.getCreatedProductIds();
            } else {
                System.out.println("Продукты найдены. Используем существующие...");
                productIds = existingIds;
            }
        logger.info("Создано {} продуктов. ID: {}", productIds.size(), productIds);
        logger.info("📦 Начало теста");
//        this.productIds = new ArrayList<>();
    }

    @Test(description = "Продукты с чётными ID недоступны для получения")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: доступ к продукту с чётным ID должен быть запрещён (403). "
            + "Если сервер вернёт 500 — это известный баг BUG-QA1-01.")
    @Issue("BUG-QA1-01")
    public void testEvenIdIsUnavailable() {
        // Arrange: находим первый чётный ID среди сгенерированных продуктов
        Long evenId = productIds.stream()
                .filter(id -> id % 2 == 0)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("❌ Не найден чётный ID среди сгенерированных продуктов"));

        logger.info("🔍 Тест BUG-QA1-01: Проверка недоступности продукта с чётным ID: {}", evenId);

        // Act: делаем GET-запрос к продукту с чётным ID
        Response response = productClient.getProductById(evenId);

        // Assert: проверяем, что API вернул 403 или известную 500 ошибку
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-01");
    }

    @Test(description = "Нельзя обновить продукт с ID, кратным 3")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: обновление продукта с ID, кратным 3, должно быть запрещено (403). "
            + "Если API вернёт 500 — это известный баг BUG-QA1-02.")
    @Issue("BUG-QA1-02")
    public void testUpdateForbiddenForIdDivisibleByThree() {
        // Arrange: ищем ID, кратный 3, среди сгенерированных продуктов
        Long id = productIds.stream().
                filter(i -> i % 3 == 0)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("❌ Не найден ID, кратный 3, среди сгенерированных продуктов"));

        Product update = new Product("Updated lov3Prod","love3", 100);
        logger.info("🔍 Тест BUG-QA1-02: Проверка запрета обновления продукта с ID {}. Обновляемые данные: {}", id, update);

        // Act: пытаемся обновить продукт с ID, кратным 3
        Response response = productClient.updateProduct(id, update);

        // Assert: ожидаем 403 или известный баг (500)
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-02");
    }

    @Test(description = "Продукты с простыми ID (2, 3, 5, 7) недоступны для получения")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: доступ к продуктам с простыми ID (2, 3, 5, 7) должен быть запрещён (403). "
            + "Если API вернёт 500 — это известный баг BUG-QA1-03.")
    @Issue("BUG-QA1-03")
    public void testPrimeIdsAreRestricted() {
        // Arrange: создаем простые числа
        int[] primeIds = {2, 3, 5, 7};

        for (int id : primeIds) {
            long longId = id; // явно приводим к long для читаемости
            logger.info("🔍 Тест BUG-QA1-03: Проверка доступа к продукту с простым ID: {}", longId);
            // Act: пытаемся обновить продукт
            Response response = productClient.getProductById(longId);
            // Assert: ожидаем 403 или известный баг (500)
            TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-03");
        }
    }

    @Test(description = "Создание продукта с ценой выше 1000 запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: при создании продукта с ценой > 1000 API должно вернуть 403 Forbidden. "
            + "Если сервер вернёт 500 — это известный баг BUG-QA1-04.")
    @Issue("BUG-QA1-04")
    public void testCreateProductWithHighPriceIsForbidden() {
        // Arrange: создаем продукт со стоимосью 1500 сом
        Product expensiveProduct = new Product("Expensive Product_" + System.currentTimeMillis(),"auto generated", 1500);
        logger.info("🔍 Тест BUG-QA1-04: попытка создать продукт с завышенной ценой: {}", expensiveProduct);

        // Act: отправляем POST-запрос на создание продукта
        Response response = productClient.createProduct(expensiveProduct);

        // Assert: проверяем, что ответ — 403, либо фиксируем баг с 500
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-04");
    }

    @Test(description = "Удаление продукта с ценой > 100 запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что удаление продукта с ценой выше 100 запрещено (403). " +
            "Если сервер возвращает 500, это известный баг BUG-QA1-05.")
    @Issue("BUG-QA1-05")
    public void testDeleteProductWithPriceOver100IsForbidden() {
        // Arrange:создаём продукт с ценой 150 — это больше порога 100, по которому удаление запрещено
        Product product = new Product("ToDelete_"+System.currentTimeMillis(), "auto delete", 150);
        Response createResponse = productClient.createProduct(product);

        // Проверка, что продукт создан
        TestUtils.assertKnownIssueOrExpected(createResponse, 200, "BUG-QA1-05");
//        TestUtils.assertKnownIssueOrExpected(createResponse, 201, "BUG-QA1-05");
        if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
            Integer id = createResponse.jsonPath().get("id");
            Assert.assertNotNull(id, "❌ Сервер не вернул поле 'id'. Ответ: " + createResponse.getBody().asString());

            logger.info("🔧 Подготовка: создан продукт с ID {} и ценой {}", id, product.getPrice());

            Response deleteResponse = productClient.deleteProduct(id.longValue());

            TestUtils.assertKnownIssueOrExpected(deleteResponse, 403, "BUG-QA1-05");
        }
    }

    @Test(description = "Изменение цены более чем на $500 запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что изменение цены продукта более чем на $500 запрещено (403). " +
            "Если сервер возвращает 500, 200 или 201 — это известная ошибка BUG-QA1-06.")
    @Issue("BUG-QA1-06")
    public void testPriceChangeMoreThan500IsForbidden() {
        // Берём первый продукт из списка
        Long id = productIds.get(0);
//        Product original = productClient.getProductById(id);
        Product original = productClient.getAllProducts().get(0);

        if (original == null) {
            throw new RuntimeException("Product with ID " + id + " not found!");
        }

        double newPrice = original.getPrice() + 550;
        Product updated = new Product(original.getName(), original.getDescription(), newPrice);


//        Product original = productClient.getAllProducts().get(0);
//        Long id = productIds.get(0);
//        Product original = productClient.getProductById(id);
//        double newPrice = original.getPrice() + 550;
//        Product updated = new Product(original.getName(), original.getDescription(), newPrice);

        logger.info("🛠️ Обновляем продукт ID {}: {} → {}", original.getId(), original.getPrice(), newPrice);
        Response response = productClient.updateProduct(original.getId(), updated);

        // Проверка с учётом багов
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-06");
    }


    @Test(description = "Проверка задержки всех операций минимум 100 мс")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет, что сервер обрабатывает операции минимум 100 мс (искусственная задержка). " +
            "Если время ответа меньше — это нарушение. Применимо ко всем методам, но здесь проверяется GET.")
    @Issue("BUG-QA1-07")
    public void testMinimumDelayOf100ms() {
        // Arrange: Убеждаемся, что есть хотя бы один продукт для запроса
        assertThat("❌ Список созданных продуктов пуст — сидер не отработал", productIds, is(not(empty())));
        Long id = productIds.get(0);
        logger.info("🧪 Начало теста BUG-QA1-07: проверка задержки для запроса GET /products/{}", id);

        // Act: Засекаем время выполнения запроса
        long start = System.currentTimeMillis();
        var response = productClient.getProductById(id);
        long duration = System.currentTimeMillis() - start;
        logger.info("⏱️ Время ответа от сервера: {} мс", duration);

        if (response.statusCode() == 500) {
            logger.error("‼️ Ожидалась задержка, но сервер упал с 500 — баг BUG-QA1-07");
            return; // баг зафиксирован
        }

        // Assert: Проверяем, что время ответа >= 100 мс
        logger.info("✅ Ожидается минимальная задержка 100 мс, фактически: {} мс", duration);
        assertThat("⏳ Время ответа должно быть не меньше 100 мс", duration, greaterThanOrEqualTo(100L));

        // Дополнительно убеждаемся, что ответ корректный
        logger.info("📦 Проверка корректности ответа: статус {}", response.getStatusCode());
        assertThat("Код ответа должен быть 200", response.getStatusCode(), is(200));
    }
}
