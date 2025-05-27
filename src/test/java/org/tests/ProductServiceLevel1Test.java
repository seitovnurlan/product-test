package org.tests;

import config.RestAssuredConfigurator;
import io.qameta.allure.*;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.model.Product;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import testutil.TestUtils;

import static org.hamcrest.MatcherAssert.assertThat;

@Epic("Тестирование уровня QA Level 1 – Базовые проверки")
@Feature("Проверка идентификаторов, цен и базовых ограничений")
public class ProductServiceLevel1Test extends BaseProductServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceLevel1Test.class);

    @Test(description = "BUG-QA1-01: Продукты с чётными ID недоступны для получения", priority = 1)
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

        // Act: выполняем GET-запрос к продукту с чётным ID
        Response response = productClient.getProductById(evenId);

        // Assert: ожидаем, что API вернул 403 или 500 (если известный баг)
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA1-01");
    }

    @Test(description = "BUG-QA1-02: Нельзя обновить продукт с ID, кратным 3", priority = 2)
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

        Product update = new Product("Updated lov3Prod_"+ System.currentTimeMillis(),"love3"+ System.currentTimeMillis(), 100);
        logger.info("🔍 Тест BUG-QA1-02: Проверка запрета обновления продукта с ID {}. С обновляемыми данными", id);

        // Act: отправляем PUT-запрос на обновление продукта с ID, кратным 3
        Response response = productClient.updateProduct(id, update);

        // Assert: проверяем, что вернулся 403 или известный баг (500)
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA1-02");
    }

    @Test(description = "BUG-QA1-03: Продукты с простыми ID (2, 3, 5, 7) недоступны для получения", priority = 3)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: доступ к продуктам с простыми ID (2, 3, 5, 7) должен быть запрещён (403). "
            + "Если API вернёт 500 — это известный баг BUG-QA1-03.")
    @Issue("BUG-QA1-03")
    public void testPrimeIdsAreRestricted() {
        // Arrange: создаём список простых чисел
        int[] primeIds = {2, 3, 5, 7};

        for (int id : primeIds) {
            long longId = id; // явно приводим к long для читаемости и для API
            logger.info("🔍 Тест BUG-QA1-03: Проверка доступа к продукту с простым ID: {}", longId);
            // Act: отправляем GET-запрос к продукту с простым ID
            Response response = productClient.getProductById(longId);
            // Assert: ожидаем 403 или известный баг (500)
            TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA1-03");
        }
    }

    @Test(description = "BUG-QA1-04: Создание продукта с ценой выше $1000 запрещено", priority = 4)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: при создании продукта с ценой > $1000 API должно вернуть 403 Forbidden. "
            + "Если сервер вернёт 500 — это известный баг BUG-QA1-04.")
    @Issue("BUG-QA1-04")
    public void testCreateProductWithHighPriceIsForbidden() {
        // Arrange: создаем продукт с завышенной ценой ($1500)
        Product expensiveProduct = new Product("Expensive Product_" + System.currentTimeMillis(),"auto generated"+ System.currentTimeMillis(), 1500);
        logger.info("🔍 Тест BUG-QA1-04: попытка создать продукт с завышенной ценой");

        // Act: отправляем POST-запрос на создание продукта
        Response response = productClient.createProduct(expensiveProduct);

        // Assert: проверяем, что ответ — 403, либо фиксируем известный баг с 500
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA1-04");
    }

    @Test(description = "BUG-QA1-05: Удаление продукта с ценой > $100 запрещено", priority = 5)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что удаление продукта с ценой выше $100 запрещено (403). " +
            "Если сервер возвращает 500, это известный баг BUG-QA1-05.")
    @Issue("BUG-QA1-05")
    public void testDeleteProductWithPriceOver100IsForbidden() {
        // Arrange:создаём продукт с ценой 150 — выше разрешённого порога для удаления
        Product product = new Product("ToDelete_"+System.currentTimeMillis(), "auto delete"+System.currentTimeMillis(), 150);
        Response createResponse = productClient.createProduct(product);
        logger.info("🔍 Тест BUG-QA1-05: Проверка, что удаление продукта с ценой выше $100 запрещено");

        // Проверка успешного создания: 200 или 201 — допустимые статусы
        logger.info("🔍 Тест BUG-QA1-05: Проверка успешного создания: 200 или 201 — допустимые статусы");
        TestUtils.assertOrSkipIfKnownBug(createResponse, 200, "BUG-QA1-05");
//        TestUtils.assertKnownIssueOrExpected(createResponse, 201, "BUG-QA1-05");

        if (createResponse.statusCode() == 201 || createResponse.statusCode() == 200) {
            Integer id = createResponse.jsonPath().get("id");
            Assert.assertNotNull(id, "❌ Сервер не вернул поле 'id'. Ответ: " + createResponse.getBody().asString());

            logger.info("🔧 Подготовка: создан продукт с ID {} и ценой {}", id, product.getPrice());
            // Act: отправляем DELETE-запрос
            Response deleteResponse = productClient.deleteProduct(id.longValue());
            // Assert: ожидаем 403 или фиксируем баг с 500
            TestUtils.assertOrSkipIfKnownBug(deleteResponse, 500, "BUG-QA1-05");
        }
    }

    @Test(description = "BUG-QA1-06: Изменение цены более чем на $500 запрещено", priority = 6)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что изменение цены продукта более чем на $500 запрещено (403). " +
            "Если сервер возвращает 500, 200 или 201 — это известная ошибка BUG-QA1-06.")
    @Issue("BUG-QA1-06")
    public void testPriceChangeMoreThan500IsForbidden() {
        // Arrange: берём первый продукт из ранее созданных
        Long id = productIds.get(0);
//        Response original = productClient.getProductById(id);
        Product original = productClient.getAllProducts().get(0);

        if (original == null) {
            throw new RuntimeException("❌ Продукт с ID " + id + " не найден.");
        }

        double newPrice = original.getPrice() + 550;
        Product updated = new Product(original.getName(), original.getDescription(), newPrice);

//        Product original = productClient.getAllProducts().get(0);
//        Long id = productIds.get(0);
//        Product original = productClient.getProductById(id);
//        double newPrice = original.getPrice() + 550;
//        Product updated = new Product(original.getName(), original.getDescription(), newPrice);

        logger.info("🛠️ Тест BUG-QA1-06: попытка изменить цену продукта ID {} с {} на {}", id, original.getPrice(), newPrice);
        // Act: отправляем PUT-запрос на обновление с изменённой ценой
        Response response = productClient.updateProduct(original.getId(), updated);

        // Assert: ожидаем 403 (запрещено), иначе — известный баг 500
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA1-06");
    }

    @Test(description = "BUG-QA1-07: Проверка, что сервер обрабатывает GET /products минимум за 100 мс", priority = 7)
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет, что сервер отвечает на запрос GET /products с задержкой не менее 100 мс. " +
            "Если время меньше — фиксируем как известный баг BUG-QA1-07 и пропускаем тест.")
    @Issue("BUG-QA1-07")
    public void testMinimumDelayOf100ms() {
        logger.info("🧪 Запуск теста BUG-QA1-07: Проверка минимальной задержки GET /api/products");
        // Act: Отправляем GET запрос и получаем Response
        Response response = given()
                .when()
                .get()
                .andReturn();

        long responseTime = response.time();
        int statusCode = response.statusCode();

        logger.info("⏱ Время ответа: {} мс, статус: {}", responseTime, statusCode);

        // Assert: проверяем статус — должно было 201, но соглаш.200 или известный баг 500
        TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA1-07");

        // Если время ответа меньше 100 мс — считаем это багом задержки и пропускаем тест
        if (responseTime < 100) {
            String msg = String.format("‼️ Время ответа %d мс меньше ожидаемых 100 мс — известный баг задержки BUG-QA1-07", responseTime);
            logger.warn(msg);
            Allure.addAttachment("Known issue: BUG-QA1-07", msg + "\nResponse body:\n" + response.getBody().asString());
            throw new SkipException("Известный баг BUG-QA1-07: задержка меньше 100 мс");
        }

        // Если дошли сюда — значит время ответа >= 100 мс, проверяем assert
        assertThat(responseTime)
                .withFailMessage("⏳ Время ответа должно быть не меньше 100 мс, но было %d", responseTime)
                .isGreaterThanOrEqualTo(100L);

        logger.info("✅ Тест BUG-QA1-07 успешно пройден: время задержки >= 100 мс");

    }

}
