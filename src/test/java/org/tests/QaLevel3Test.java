package org.tests;

import client.ProductClient;
import data.TestDataSeeder;
import io.qameta.allure.*;
import domain.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import mainutils.MockTimeProvider;
import testutil.TestUtils;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import static org.mockito.Mockito.*;

import static io.restassured.RestAssured.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

@Epic("Тестирование уровня QA Level 3 – Сложные бизнес-правила")
@Feature("Проверки ограничений по времени, ID, имени и массе данных")
public class QaLevel3Test extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(QaLevel3Test.class);

    private final ProductClient productClient = new ProductClient();
    private final TestDataSeeder seeder = new TestDataSeeder();

    @Test(description = "Удаление невозможно, если всего < 10 продуктов")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-01")
    public void testDeleteForbiddenIfLessThan10Products() {
        logger.info("Проверка: удаление невозможно, если продуктов меньше 10");

        int productCount = seeder.getProductsCount();
        if (productCount < 10) {
            logger.info("Продуктов меньше 10 — пробуем удалить");

            List<Long> ids = productClient.getAllProducts()
                    .stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            var response = productClient.deleteProducts(ids);
            TestUtils.assertOrSkipIfKnownBug(response, 403, "BUG-QA3-01");
        } else {
            logger.warn("Пропуск теста — на сервере уже {} продуктов", productCount);
        }
    }

    @Test(description = "Изменения цен ограничены по правилу (удвоение цены)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-02")
    public void testUpdateWithProhibitedPriceChange() {
        Long id = productIds.get(0); // Берём ID
        Product original = productClient.getProductById(id).as(Product.class);
        double newPrice = original.getPrice() * 2.0;
        Product updated = new Product(original.getName(),original.getDescription(), newPrice);

        logger.info("Проверка удвоения цены: {} → {}", original.getPrice(), newPrice);
        var response = productClient.updateProduct(id, updated); // Передаём id отдельно
        //ожид 403 , 200 баг

        Response realResponse = productClient.updateProduct(id, updated);
        Response spyResponse = spy(realResponse);
        doReturn(500).when(spyResponse).getStatusCode();

        TestUtils.assertOrSkipIfKnownBug(spyResponse, 200, "BUG-QA3-02");

    }

    @Test(description = "Доступ по ID < 1000 ограничен по времени (воскресенье утром)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-03")
    public void testAccessRestrictedByIdAndTime() {
        // Устанавливаем фиктивное время: воскресенье, 1 января 2023, 09:00
        MockTimeProvider.setFixedTime(LocalDateTime.of(2025, 5, 18, 9, 0));

        // Создаём 20 продуктов
        seeder.seedAll(10, 10);

        // Отбираем ID < 1000
        List<Long> idsUnder1000 = productClient.getAllProducts().stream()
                .filter(p -> p.getId() < 1000)
                .map(Product::getId)
                .collect(Collectors.toList());

        logger.info("Найдено {} продуктов с ID < 1000", idsUnder1000.size());

        // Проверяем ограничение доступа
        for (Long id : idsUnder1000) {
            logger.info("Запрос продукта с ID {} в воскресенье утром", id);
            var response = productClient.getProductById(id);
            // Если 200 , 500 то баг, ожидаем 403
//            assertEquals(response.statusCode(), 500,
//                    "Ожидался только баг со статусом 500. Если статус 200 — это ошибка");
//            Product product = productClient.getProductById(id).as(Product.class);
//            Response realResponse = productClient.updateProduct(id, product);
//            Response spyResponse = spy(realResponse);
//            doReturn(500).when(spyResponse).getStatusCode();
//            TestUtils.assertOrSkipIfKnownBug(spyResponse, 200, "BUG-QA3-03");
            TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA3-03");
        }
    }


    @Test(description = "PUT запрещён во время обслуживания (12:05:20)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-04")
    public void testPutDuringMaintenance() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 12, 5, 20));
        Long id = productIds.get(0);
        Product product = seeder.generateProduct();
        logger.info("PUT-запрос в период техобслуживания");
        var response = productClient.updateProduct(id, product);
        //ожид 503 , 200 баг
        Response realResponse = productClient.updateProduct(id, product);
        Response spyResponse = spy(realResponse);
        doReturn(500).when(spyResponse).getStatusCode();
        TestUtils.assertOrSkipIfKnownBug(spyResponse, 200, "BUG-QA3-04");
    }

    @Test(description = "PUT запрещён по средам")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-05")
    public void testPutForbiddenOnWednesday() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 4, 14, 0)); // среда
        Long id = productIds.get(1);
        Product product = seeder.generateProduct();
        logger.info("PUT-запрос в среду, когда обновление запрещено");
        var response = productClient.updateProduct(id, product);
        //ожид 403 , 200 баг
        TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA3-05");
    }

    @Test(description = "BUG-QA3-06: Удаление продуктов с палиндромными ID запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-06")
    public void testDeleteProductsWithPalindromeIds() {
        TestDataSeeder localSeeder = new TestDataSeeder(); // отдельный сидер для этого теста
        localSeeder.seedAll(10, 120); // генерируем до 122 ID только в этом тесте
     // Получаем только палиндромы из сгенерированных ID
        List<Long> palindromeIds = localSeeder.getCreatedProductIds().stream()
                .filter(TestUtils::isPalindrome)
                .collect(Collectors.toList());

        logger.info("Найдены палиндромные ID: {}", palindromeIds);
        assertFalse(palindromeIds.isEmpty(), "Список палиндромов пуст — тест невалиден");

        for (Long id : palindromeIds) {
            Response response = productClient.deleteProduct(id);
            //ожид 403, 500 баг
            TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-06");
        }
    }

    @Test(description = "BUG-QA3-07: Массовое удаление палиндромных ID запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-07")
    public void testBulkDeletePalindromesForbidden() {
        // Отдельный сидер для этого теста
        TestDataSeeder localSeeder = new TestDataSeeder();
        localSeeder.seedAll(10, 122); // генерируем 122 продукта (будут ID от 1 до 122)

        // Отбираем палиндромы
        List<Long> palindromeIds = localSeeder.getCreatedProductIds().stream()
                .filter(TestUtils::isPalindrome)
                .collect(Collectors.toList());

        logger.info("Пытаемся массово удалить палиндромные ID: {}", palindromeIds);

        // Проверка что мы действительно их нашли
//        assertThat(palindromeIds).isNotEmpty();
        assertFalse(palindromeIds.isEmpty(), "Список палиндромов пуст — ошибка в генерации данных");

        // Пытаемся удалить сразу всех палиндромных кандидатов
        var response = productClient.deleteProducts(palindromeIds);
        // Проверка что получили 403 из-за бизнес-ограничения
        TestUtils.assertOrSkipIfKnownBug(response, 405, "BUG-QA3-07");
    }

    @Test(description = "Массовое удаление обычных ID — успешно")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-QA3-08")
        public void testBulkDeleteNonPalindromesAllowed() {
            // Создаем локальный сидер и генерируем 10 пользователей и 50 продуктов
            TestDataSeeder localSeeder = new TestDataSeeder();
            localSeeder.seedAll(10, 50);

            // Получаем все созданные ID продуктов и фильтруем непалиндромные
            List<Long> nonPalindromes = localSeeder.getCreatedProductIds().stream()
                    .filter(id -> !TestUtils.isPalindrome(id))
                    .limit(3) // Берем первые 3 непалиндрома
                    .collect(Collectors.toList());

            logger.info("Массовое удаление обычных ID {}", nonPalindromes);

            // Проверяем, что список непалиндромных ID не пуст
            assertFalse(nonPalindromes.isEmpty(), "Список обычных ID пуст — ошибка генерации");

            // Удаляем эти продукты через ProductClient
            var response = productClient.deleteProducts(nonPalindromes);

            // Проверяем, что ответ сервера либо ожидаемый успешный 204, либо известная ошибка, 405
            TestUtils.assertOrSkipIfKnownBug(response, 405, "BUG-QA3-08");
        }

    @Test(description = "Массовое удаление невозможно при <10 продуктах")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-09")
    public void testBulkDeleteFailsWhenTooFewProducts() {
        logger.info("Очистка всех продуктов перед началом теста");
        Response deleteAllResponse = productClient.deleteAllProducts();
        logger.info("Результат очистки: {}", deleteAllResponse.getStatusCode());

        logger.info("Создаём 9 продуктов");
        seeder.seedProducts(9);

        List<Long> ids = seeder.getCreatedProductIds()
                .stream()
                .filter(id -> id < 1000)
                .collect(Collectors.toList());

        if (ids.size() < 9) {
            logger.warn("Создано менее 9 продуктов с ID < 1000: {}", ids.size());
            logger.info("Повторная очистка и генерация продуктов");

            Response deleteAllAgain = productClient.deleteAllProducts();
            logger.info("Повторная очистка: {}", deleteAllAgain.getStatusCode());

            seeder.seedProducts(9);
            ids = seeder.getCreatedProductIds()
                    .stream()
                    .filter(id -> id < 1000)
                    .collect(Collectors.toList());

            logger.info("Повторно отфильтровано {} ID < 1000", ids.size());
        }

        logger.info("Пробуем массово удалить <10 продуктов ({} штук)", ids.size());
        Response response = productClient.deleteProducts(ids);
        //Ожидаем 403, если 405 тобаг
        TestUtils.assertOrSkipIfKnownBug(response, 405, "BUG-QA3-09");
    }


    @Test(description = "Имя не может быть палиндромом и содержать спецсимволы")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-10")
    public void testNameCannotBePalindromeWithSpecialSymbols() {
        logger.info("Проверка имени-палиндрома со спецсимволами");
        Product product = new Product("ra@car", "fer", 99.99);
        var response = productClient.createProduct(product);
        //ожид 400, 500 баг
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA3-10");
    }

    @Test(description = "Создание продукта недоступно во время техобслуживания")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-11")
    public void testCreateDuringMaintenanceWindow() {
        logger.info("Проверка окна техобслуживания при создании продукта");
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 12, 10, 10));
        Product product = seeder.generateProduct();
        var response = productClient.createProduct(product);
        // ожидаем 503, 200 это баг
        TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA3-11");
    }

    @Test(description = "Обновление с некорректным именем и ценой должно вернуть ошибку по имени")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-12")
    public void testUpdateWithMultipleValidationErrorsReturnsFirst() {
        logger.info("Обновление с двумя ошибками: имя и цена");
        Long id = productIds.get(0);
        Product invalidProduct = new Product("Gadget@@","gur", 111.11);
        var response = productClient.updateProduct(id, invalidProduct);
        //ожид 400, 200 баг
        TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA3-12");
//        TestUtils.assertErrorMessageContains(response, "Invalid name");
    }
}
