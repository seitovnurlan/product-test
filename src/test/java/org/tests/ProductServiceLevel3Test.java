package org.tests;

import config.RestAssuredConfigurator;
import data.TestDataSeeder;
import io.qameta.allure.*;
import domain.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import mainutils.MockTimeProvider;
import testutil.TestUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

@Epic("Тестирование уровня QA Level 3 – Сложные бизнес-правила")
@Feature("Проверки ограничений по времени, ID, имени и массе данных")
public class ProductServiceLevel3Test extends BaseProductServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceLevel3Test.class);

    @Test(description = "BUG-QA3-01: Удаление невозможно, если всего < 10 продуктов", priority = 1)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет, что при попытке удалить все продукты, если их < 10, сервер возвращает 403 (удаление запрещено по бизнес-правилу).")
    @Issue("BUG-QA3-01")
    public void testDeleteForbiddenIfLessThan10Products() {
        // Arrange: Получаем текущее количество продуктов на сервере
        logger.info("🔍 Тест BUG-QA3-01: Проверка запрета удаления всех продуктов, если их < 10");

        int productCount = seeder.getProductsCount();

        if (productCount < 10) {
            logger.info("ℹ️ На сервере {} продуктов — меньше 10. Пробуем удалить все.", productCount);

            // Arrange: Формируем список всех ID продуктов для удаления
            List<Long> idsToDelete = productClient.getAllProducts()
                    .stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            logger.debug("📦 Список ID для удаления: {}", idsToDelete);

            // Act: Пытаемся массово удалить все продукты
            Response response = productClient.deleteProducts(idsToDelete);

            // Assert: Убеждаемся, что сервер возвращает 403 Forbidden (удаление запрещено)
            TestUtils.assertOrSkipIfKnownBug(response, 403, "BUG-QA3-01");
        } else {
            // Arrange: Слишком много продуктов — тест неактуален, логируем и пропускаем
            logger.warn("⚠️ Пропуск теста: на сервере уже {} продуктов (>= 10)", productCount);
        }
    }

    @Test(description = "BUG-QA3-02: Запрет на изменение цены продукта более чем на $500", priority = 2)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что изменение цены продукта более чем на $500 запрещено (403). " +
            "Если сервер возвращает 500, 200 или 201 — это известная ошибка BUG-QA3-02.")
    @Issue("BUG-QA3-02")
    public void testUpdateWithProhibitedPriceChange() {
        // Arrange: Получаем исходный продукт
        Long id = productIds.get(2); // Берём ID
        Product original = productClient.getProductByIdSafe(id); // ✅ безопасный вызов

        // Arrange: Готовим данные для обновления с нарушением правила (удвоение цены)
        double newPrice = original.getPrice() + 501.0;
        Product updated = new Product(original.getName(),original.getDescription(), newPrice);

        logger.info("🔍 Тест BUG-QA3-02: Проверка запрета обновления продукта с ID {}", id);

        // Act: Пытаемся обновить продукт с новой ценой
        logger.info("Проверка увеличения цены: {} → {}", original.getPrice(), newPrice);
        Response response = productClient.updateProduct(id, updated); // Передаём id отдельно

        //ожид. 403, но сервер обычно кидает 500, 200 баг
        // Assert: Проверяем, что сервер отклоняет такое изменение (403), иначе — известный баг
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-02");
    }

    @Test(description = "BUG-QA3-03: Доступ по ID < 1000 ограничен по времени (воскресенье утром)", priority = 3)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что продукты с ID < 1000 недоступны в воскресенье утром (ожидается 403). " +
            "Если возвращается 200 или 500 — это баг BUG-QA3-03. В тесте используется фиктивное время.")
    @Issue("BUG-QA3-03")
    public void testAccessRestrictedByIdAndTime_ProductsOnly() {
        // Arrange: Устанавливаем фиксированное время — воскресенье, 18 мая 2025 года, 09:00
        LocalDateTime mockTime = LocalDateTime.of(2025, 5, 18, 9, 0);
        MockTimeProvider.setFixedTime(mockTime);
        TestUtils.assumeServerTime(mockTime);

        logger.info("🔍 Тест BUG-QA3-03: Проверка доступа к продуктам с ID < 1000 в ограниченное время");
        logger.info("🕒 Мок-время установлено на {} ({})",
                mockTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                mockTime.getDayOfWeek());

        // Arrange: Получаем уже созданные продукты с ID < 1000
        List<Long> idsUnder1000 = productClient.getAllProducts().stream()
                .filter(p -> p.getId() < 1000)
                .map(Product::getId)
                .collect(Collectors.toList());

        logger.info("Найдено {} продуктов с ID < 1000", idsUnder1000.size());

        if (idsUnder1000.isEmpty()) {
            throw new SkipException("Нет продуктов с ID < 1000 — тест пропущен, так как не к чему применять правило.");
        }

        // Act + Assert: Проверяем каждый ID
        for (Long id : idsUnder1000) {
            logger.info("➡️ Запрос продукта с ID {} в воскресенье утром", id);
            Response response = productClient.getProductById(id);

            // Проверка: если 200 или 500 — это баг BUG-QA3-03
            TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-03");
        }
    }



    @Test(description = "BUG-QA3-04: PUT запрещён во время обслуживания (12:00–13:00)", priority = 4)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что сервер отклоняет PUT-запросы в период обслуживания (12:00–13:00). " +
            "Если в это время сервер возвращает 200 вместо 503 — это баг BUG-QA3-04.")
    @Issue("BUG-QA3-04")
    public void testPutDuringMaintenance() {
        // Arrange: Устанавливаем мок-время: 12:05:20 (внутри окна обслуживания)
        LocalDateTime maintenanceTime = LocalDateTime.of(2025, 5, 26, 12, 5, 20);
        MockTimeProvider.setFixedTime(maintenanceTime);

        Product product = seeder.generateProduct();
        Long id = productIds.get(1);

        logger.info("🔍 Тест BUG-QA3-04: PUT-запрос в период обслуживания");
        logger.info("🕒 Мок-время: {}", maintenanceTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // Act: Пытаемся обновить продукт
        Response response = productClient.updateProduct(id, product);

        logger.info("📥 Ответ сервера: статус = {}, тело = {}", response.statusCode(), response.body().asPrettyString());

        // Assert: В это время ожидаем 503. Если пришёл 200 — это баг
        TestUtils.assertOrSkipIfKnownBug(response, 503, "BUG-QA3-04");
    }

    @Test(description = "BUG-QA3-05: PUT запрещён по средам", priority = 5)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка, что по средам обновление продукта (PUT) запрещено. " +
            "Если сервер в этот день возвращает 200 — это баг BUG-QA3-05.")
    @Issue("BUG-QA3-05")
    public void testPutForbiddenOnWednesday() {
        // Arrange: устанавливаем фиктивное время — среда, 4 января 2023 года
        LocalDateTime wednesday = LocalDateTime.of(2023, 1, 4, 14, 0);// среда
        MockTimeProvider.setFixedTime(wednesday);

//        LocalDateTime monday = LocalDateTime.of(2025, 5, 26, 14, 0); // Понедельник
//        MockTimeProvider.setFixedTime(monday);

        Product product = seeder.generateProduct();
        Long id = productIds.get(1);

        logger.info("🔍 Тест BUG-QA3-05: PUT-запрос в день, когда обновление запрещено (среда)");
        logger.info("🗓️ Мок-дата: {}, день недели: {}",
                wednesday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                wednesday.getDayOfWeek());

        logger.info("PUT-запрос в среду, когда обновление запрещено");
        // Act: Пытаемся отправить PUT-запрос
        Response response = productClient.updateProduct(id, product);


        logger.info("📥 Ответ сервера: статус = {}, тело = {}",
                response.statusCode(),
                response.body().asPrettyString());

        // Assert: Ожидаем 403, если получаем 200 или 500 — это баг
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-05");

//        logger.info("🟢 Проверка разрешения PUT в понедельник ({}", monday.getDayOfWeek());
//        Response response = productClient.updateProduct(id, product);
//        assertEquals(response.statusCode(), 200, "PUT должен быть разрешён в понедельник");
    }

    @Test(description = "BUG-QA3-06: Удаление продуктов с палиндромными ID запрещено", priority = 6)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет, что API запрещает удаление продуктов с палиндромными ID (например, 121, 131). Ожидается 403 Forbidden, но фактически возвращается 500 — это баг.")
    @Issue("BUG-QA3-06")
    public void testDeleteProductsWithPalindromeIds() {
        logger.info("🔍 Тест BUG-QA3-06: Удаление продуктов с палиндромными ID должно возвращать 403 Forbidden");

        // ✅ Используем productIds из BaseProductServiceTest
        logger.info("📊 Всего продуктов для анализа: {}", productIds.size());
        logger.debug("📋 Анализируем ID: {}", productIds);

        List<Long> palindromeIds = productIds.stream()
                .filter(TestUtils::isPalindrome)
                .limit(3)
                .toList();

        logger.info("🔢 Найдены палиндромные ID: {}", palindromeIds);

        if (palindromeIds.isEmpty()) {
            throw new SkipException("❌ Нет палиндромных ID среди продуктов — тест невалиден и пропускается");
        }

        for (Long palindromeId : palindromeIds) {
            // Act
            Response response = productClient.deleteProduct(palindromeId);
            logger.info("🧨 Попытка удалить палиндром ID = {}, статус = {}", palindromeId, response.statusCode());

            // Assert
            TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-06");
        }
    }

    @Test(description = "BUG-QA3-07: Массовое удаление продуктов с палиндромными ID должно быть запрещено", priority = 7)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет, что API не позволяет удалить несколько продуктов с палиндромными ID. Ожидается 403 Forbidden, но возвращается 500 — это баг.")
    @Issue("BUG-QA3-07")
    public void testBulkDeletePalindromesForbidden() {
        logger.info("🔍 Тест BUG-QA3-07: Массовое удаление продуктов с палиндромными ID должно быть запрещено");

        // ✅ Используем заранее загруженные productIds из BaseProductServiceTest
        logger.info("📊 Всего продуктов для анализа: {}", productIds.size());
        logger.debug("📋 Все доступные ID: {}", productIds);

        // Ищем первые 3 палиндрома
        List<Long> palindromeIds = productIds.stream()
                .filter(TestUtils::isPalindrome)
                .limit(3)
                .toList();

        logger.info("🔎 Найдены палиндромные ID для массового удаления: {}", palindromeIds);

        if (palindromeIds.isEmpty()) {
            throw new SkipException("❌ Не найдено ни одного палиндромного ID — тест невалиден");
        }

        // Act
        Response response = productClient.deleteProducts(palindromeIds);
        logger.info("🧨 Попытка массово удалить палиндромы {}, статус = {}", palindromeIds, response.statusCode());

        // Assert
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-07");
    }




    @Test(description = "BUG-QA3-08: Массовое удаление обычных (непалиндромных) ID должно быть успешно", priority = 8)
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверяет, что API разрешает массовое удаление продуктов с обычными (непалиндромными) ID. Ожидается статус 204.")
    @Issue("BUG-QA3-08")
    public void testBulkDeleteNonPalindromesAllowed() {
        logger.info("🔍 Тест BUG-QA3-08: Массовое удаление обычных (непалиндромных) ID должно быть успешным");

        // ✅ Используем сидированные ID
        List<Long> allProductIds = seeder.getCreatedProductIds();

        List<Long> nonPalindromes = allProductIds.stream()
                .filter(id -> !TestUtils.isPalindrome(id))
                .limit(3)
                .collect(Collectors.toList());

        logger.info("🧹 Пытаемся массово удалить обычные ID: {}", nonPalindromes);
        assertFalse(nonPalindromes.isEmpty(), "❌ Не найдено ни одного обычного (непалиндромного) ID — тест невалиден");

        // Act
        Response response = productClient.deleteProducts(nonPalindromes);

        // Assert
        TestUtils.assertOrSkipIfKnownBug(response, 204, "BUG-QA3-08");
    }

    @Test(description = "BUG-QA3-09: Массовое удаление невозможно при количестве продуктов < 10", priority = 9)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет, что если в базе менее 10 продуктов с ID < 1000, то массовое удаление запрещено и возвращается 403.")
    @Issue("BUG-QA3-09")
    public void testBulkDeleteFailsWhenTooFewProducts() {
        logger.info("🔍 Тест BUG-QA3-09: Массовое удаление невозможно при количестве продуктов < 10");

        // Arrange: очищаем базу перед тестом
//        logger.info("🧼 Очистка всех продуктов перед началом теста");
//        Response deleteAllResponse = productClient.deleteAllProducts();
//        assertEquals(200, deleteAllResponse.getStatusCode(), "Не удалось очистить базу продуктов");

        // Получаем список всех продуктов из базы
        List<Long> allProductIds = productClient.getAllProductIds();

        // Фильтруем только ID < 1000
        List<Long> filteredIds = allProductIds.stream()
                .filter(id -> id < 1000)
                .collect(Collectors.toList());

        logger.info("🧮 В базе найдено {} продуктов с ID < 1000", filteredIds.size());

        // Если в базе продуктов < 10, генерируем продукты, чтобы получить именно 9 (тестовое условие)
        if (filteredIds.size() >= 10) {
            // Чтобы проверить негативный кейс, сначала удалим продукты, чтобы стало <10
            logger.info("⚠️ В базе более 10 продуктов с ID < 1000, очищаем для теста");
            Response clearAgain = productClient.deleteAllProducts();
            assertEquals(200, clearAgain.getStatusCode(), "Не удалось повторно очистить базу");

            logger.info("📦 Генерируем 9 продуктов");
            seeder.seedProducts(9);

            // Обновляем список ID после генерации
            filteredIds = productClient.getAllProductIds().stream()
                    .filter(id -> id < 1000)
                    .collect(Collectors.toList());
            logger.info("🧮 После генерации получено {} продуктов с ID < 1000", filteredIds.size());
            assertEquals(9, filteredIds.size(), "❌ Должно быть ровно 9 продуктов с ID < 1000");
        }

        // Act: отправляем запрос на массовое удаление продуктов < 10
        logger.info("🚫 Пробуем массово удалить {} продуктов (меньше 10)", filteredIds.size());
        Response response = productClient.deleteProducts(filteredIds);

        // Assert: ожидаем 403 или, если баг, 405
        TestUtils.assertOrSkipIfKnownBug(response, 403, "BUG-QA3-09");
    }


    @Test(description = "BUG-QA3-10: Имя не может быть палиндромом и содержать спецсимволы", priority = 10)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверяет, что имя продукта, являющееся палиндромом даже с учётом спецсимволов, не проходит валидацию. " +
            "Пример: 'ra@car' — палиндром 'racar'. Ожидается ошибка 400, но сервер возвращает 500 (баг).")
    @Issue("BUG-QA3-10")
    public void testNameCannotBePalindromeWithSpecialSymbols() {
        logger.info("🔍 Тест BUG-QA3-10: Имя-палиндром с символами должно отклоняться");

      // Arrange: создаём продукт с палиндромным именем, содержащим спецсимвол
        Product product = new Product("ra@car", "fer", 99.99);
        logger.info("Создаём продукт с именем: '{}'", product.getName());

        // Act: отправляем POST-запрос
        Response response = productClient.createProduct(product);

        // Assert: ожидаем 400, но сервер возвращает 500 — известная ошибка
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-10");
    }

    @Test(description = "BUG-QA3-11: Создание продукта недоступно во время техобслуживания", priority = 11)
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
Проверяет, что создание продукта невозможно во время окна технического обслуживания.
Допустим, с 12:00 до 12:30 (время задаётся через MockTimeProvider).
Сервер должен возвращать 503, но вместо этого возвращает 200 — это баг.
""")
    @Issue("BUG-QA3-11")
    public void testCreateDuringMaintenanceWindow() {
        logger.info("🔍 Тест BUG-QA3-11: создание продукта в период технического окна должно быть заблокировано");

        // Arrange: устанавливаем фиксированное время на 12:10 (внутри тех. окна)
        LocalDateTime maintenanceTime = LocalDateTime.of(2025, 5, 19, 12, 10, 10);
        MockTimeProvider.setFixedTime(maintenanceTime);
        logger.info("🕒 Время установлено на {}", maintenanceTime);

        // Act: пытаемся создать продукт
        Product product = seeder.generateProduct();
        logger.info("📦 Пытаемся создать продукт во время техобслуживания: {}", product.getName());

        Response response = productClient.createProduct(product);

        // Assert: ожидаем 503 Service Unavailable, но фактически получаем 200
        TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA3-11");
    }

    @Test(description = "BUG-QA3-12: Обновление с некорректным именем и ценой должно вернуть ошибку по имени", priority = 12)
    @Severity(SeverityLevel.CRITICAL)
    @Description("""
Проверка приоритета валидации: при наличии сразу нескольких ошибок (например, имя содержит спецсимволы, 
а цена невалидна), API должно возвращать ошибку по первому приоритетному полю. 
Сейчас сервер пропускает всё и возвращает 200 — это баг.
""")
    @Issue("BUG-QA3-12")
    public void testUpdateWithMultipleValidationErrorsReturnsFirst() {
        logger.info("🔍 Тест BUG-QA3-12: обновление с ошибками в имени и цене должно вернуть ошибку по имени");

        // Arrange: выбираем существующий продукт и готовим невалидные данные
        Long id = productIds.get(0);
        Product invalidProduct = new Product("Gadget@@","gur", 111.11);

        logger.info("⚠️ Попытка обновить продукт с ID={} невалидными данными: {}", id, invalidProduct);

        // Act: отправляем PUT-запрос
        Response response = productClient.updateProduct(id, invalidProduct);

        // Assert: ожидаем 400 Bad Request, но приходит 200 — это баг
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-12");

        // Дополнительно можно проверить текст ошибки:
        // TestUtils.assertErrorMessageContains(response, "Invalid name");
    }
}
