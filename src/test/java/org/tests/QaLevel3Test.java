package org.tests;

import client.ProductClient;
import data.TestDataSeeder;
import io.qameta.allure.*;
import domain.model.Product;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import io.restassured.response.Response;
import org.testng.asserts.Assertion;
import utils.MockTimeProvider;
import utils.TestUtils;
import java.util.concurrent.TimeUnit;
import java.util.List;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Epic("Тестирование уровня QA Level 3 – Сложные бизнес-правила")
@Feature("Проверки ограничений по времени, ID, имени и массе данных")
public class QaLevel3Test extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(QaLevel3Test.class);

    private final ProductClient productClient = new ProductClient();
    private final TestDataSeeder seeder = new TestDataSeeder();
    private List<Long> productIds;

    @BeforeClass
    public void setup() {
        logger.info("Сидирование мок-данных перед тестами уровня 3");
        seeder.seedAll();
        productIds = seeder.getCreatedProductIds();
        logger.info("Создано {} продуктов. ID: {}", productIds.size(), productIds);
    }

    @Test(description = "Удаление невозможно, если всего < 10 продуктов")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-01")
    public void testDeleteForbiddenIfLessThan10Products() {
        logger.info("Проверка: удаление невозможно, если продуктов меньше 10");

        int productCount = seeder.getProductsCount();
        if (productCount < 10) {
            logger.info("Продуктов меньше 10 — пробуем удалить");

            List<Integer> ids = productClient.getAllProducts()
                    .stream()
                    .map(Product::getId)
                    .collect(Collectors.toList());

            var response = productClient.deleteProducts(ids);
            TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-01");
        } else {
            logger.warn("Пропуск теста — на сервере уже {} продуктов", productCount);
        }
    }

    @Test(description = "Изменения цен ограничены по правилу (удвоение цены)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-02")
    public void testUpdateWithProhibitedPriceChange() {
        int id = productIds.get(0).intValue();
        var original = productClient.getProduct(id).as(Product.class);
        double newPrice = original.getPrice() * 2.0;
        Product updated = new Product(original.getName(), newPrice);

        logger.info("Проверка удвоения цены: {} → {}", original.getPrice(), newPrice);
        var response = productClient.updateProduct(id, updated);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-02");
    }

    @Test(description = "Доступ по ID < 1000 ограничен по времени (воскресенье утром)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-03")
    public void testAccessRestrictedByIdAndTime() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 9, 0)); // воскресенье
        int id = 999;
        logger.info("Запрос продукта с ID {} в воскресенье утром", id);
        var response = productClient.getProduct(id);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-03");
    }

    @Test(description = "PUT запрещён во время обслуживания (12:05:20)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-04")
    public void testPutDuringMaintenance() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 12, 5, 20));
        int id = productIds.get(0);
        Product product = seeder.generateProduct();
        logger.info("PUT-запрос в период техобслуживания");
        var response = productClient.updateProduct(id, product);
        TestUtils.assertKnownIssueOrExpected(response, 503, "BUG-QA3-04");
    }

    @Test(description = "PUT запрещён по средам")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-05")
    public void testPutForbiddenOnWednesday() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 4, 14, 0)); // среда
        int id = productIds.get(1);
        Product product = seeder.generateProduct();
        logger.info("PUT-запрос в среду, когда обновление запрещено");
        var response = productClient.updateProduct(id, product);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-05");
    }

    @Test(description = "Удаление палиндромного ID запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-06")
    public void testDeletePalindromeIdForbidden() {
        int palindromeId = seeder.seedProductWithId(121);
        logger.info("Проверка удаления палиндромного ID {}", palindromeId);
        var response = productClient.deleteProduct(palindromeId);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-06");
    }

    @Test(description = "Массовое удаление палиндромных ID запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-07")
    public void testBulkDeletePalindromesForbidden() {
        List<Integer> ids = List.of(121, 131, 141);
        seeder.seedSpecificIds(ids);
        logger.info("Массовое удаление палиндромных ID {}", ids);
        var response = productClient.deleteProducts(ids);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-07");
    }

    @Test(description = "Массовое удаление обычных ID — успешно")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-QA3-08")
    public void testBulkDeleteNonPalindromesAllowed() {
        List<Integer> ids = List.of(101, 105, 109);
        seeder.seedSpecificIds(ids);
        logger.info("Массовое удаление обычных ID {}", ids);
        var response = productClient.deleteProducts(ids);
        TestUtils.assertKnownIssueOrExpected(response, 204, "BUG-QA3-08");
    }

    @Test(description = "Массовое удаление невозможно при <10 продуктах")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-09")
    public void testBulkDeleteFailsWhenTooFewProducts() {
        seeder.clearAll();
        seeder.seedProducts(9);
        var ids = seeder.getCreatedProductIds();
        logger.info("Повторная проверка ограничения массового удаления при <10 продуктах");
        var response = productClient.deleteProducts(ids);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA3-09");
    }

    @Test(description = "Имя не может быть палиндромом и содержать спецсимволы")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-10")
    public void testNameCannotBePalindromeWithSpecialSymbols() {
        logger.info("Проверка имени-палиндрома со спецсимволами");
        Product product = new Product("ra@car", 99.99);
        var response = productClient.createProduct(product);
        TestUtils.assertKnownIssueOrExpected(response, 400, "BUG-QA3-10");
    }

    @Test(description = "Создание продукта недоступно во время техобслуживания")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-11")
    public void testCreateDuringMaintenanceWindow() {
        logger.info("Проверка окна техобслуживания при создании продукта");
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 12, 10, 10));
        Product product = seeder.generateProduct();
        var response = productClient.createProduct(product);
        TestUtils.assertKnownIssueOrExpected(response, 503, "BUG-QA3-11");
    }

    @Test(description = "Обновление с некорректным именем и ценой должно вернуть ошибку по имени")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA3-12")
    public void testUpdateWithMultipleValidationErrorsReturnsFirst() {
        logger.info("Обновление с двумя ошибками: имя и цена");
        int id = productIds.get(0);
        Product invalidProduct = new Product("Gadget@@", 111.11);
        var response = productClient.updateProduct(id, invalidProduct);
        TestUtils.assertKnownIssueOrExpected(response, 400, "BUG-QA3-12");
        TestUtils.assertErrorMessageContains(response, "Invalid name");
    }
}
