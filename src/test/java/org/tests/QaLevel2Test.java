package org.tests;

import data.TestDataSeeder;
import io.qameta.allure.*;
import domain.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import testutils.MockTimeProvider;
import client.ProductClient;
import testutils.TestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.testng.Assert.assertEquals;

@Epic("Тестирование уровня QA Level 2 – Расширенные проверки")
@Feature("Проверка обработки ошибок при создании, удалении и валидации продуктов")
public class QaLevel2Test extends BaseTest {

    private static final Logger logger = LoggerFactory.getLogger(QaLevel2Test.class);

    private final ProductClient productClient = new ProductClient();
    private final TestDataSeeder seeder = new TestDataSeeder();
    private List<Long> productIds;

    @BeforeClass
    public void setup() {
        logger.info("Сидирование мок-данных перед тестами уровня 2");
        seeder.seedAll();
        productIds = seeder.getCreatedProductIds();
        logger.info("Создано {} продуктов. ID: {}", productIds.size(), productIds);
    }

    @Test(description = "Обновления запрещены ночью (22:00–06:00)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA2-01")
    public void testUpdateForbiddenAtNight() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 23, 0));
        Long id = productIds.get(0);
        Product update = seeder.generateProduct();

        var response = productClient.updateProduct(id, update);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA2-01");
    }

    @Test(description = "Удаления запрещены по понедельникам до 09:00")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA2-02")
    public void testDeleteForbiddenOnMondayMorning() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 2, 8, 30)); // Понедельник
        Long id = productIds.get(1);

        var response = productClient.deleteProduct(id);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA2-02");
    }

    @Test(description = "Окно обслуживания: каждую 5-ю минуту и если секунды < 30")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-QA2-03")
    public void testMaintenanceWindowReturns503() {
        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 12, 5, 10));
        Long id = productIds.get(2);

        var response = productClient.getProductById(id);
        TestUtils.assertKnownIssueOrExpected(response, 503, "BUG-QA2-03");
    }

//    @Test(description = "В воскресенье утром доступны только ID > 1000")
//    @Severity(SeverityLevel.NORMAL)
//    @Issue("BUG-QA2-04")
//    public void testSundayMorningAccessOnlyForHighIds() {
//        MockTimeProvider.setFixedTime(LocalDateTime.of(2023, 1, 1, 8, 0)); // Воскресенье
//
//        var lowIdResponse = productClient.getProductById(900);
//        TestUtils.assertKnownIssueOrExpected(lowIdResponse, 403, "BUG-QA2-04");
//
//        var highIdResponse = productClient.getProductById(1001);
//        assertEquals(highIdResponse.getStatusCode(), 200);
//    }

    @Test(description = "Названия не могут содержать спецсимволы (!@#...)")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA2-05")
    public void testNameWithSpecialCharactersIsRejected() {
        Product product = seeder.generateProduct();
        product.setName("Invalid@Name!");

        var response = productClient.createProduct(product);
        TestUtils.assertKnownIssueOrExpected(response, 400, "BUG-QA2-05");
    }

    @Test(description = "Названия-палиндромы зарезервированы")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA2-06")
    public void testPalindromeNameConflict() {
        Product product = seeder.generateProduct();
        product.setName("racecar");

        var response = productClient.createProduct(product);
        TestUtils.assertKnownIssueOrExpected(response, 409, "BUG-QA2-06");
    }

    @Test(description = "Не более 5 операций с одним и тем же названием")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA2-07")
    public void testTooManyRequestsWithSameName() {
        String commonName = "Gadget";
        Product product = seeder.generateProduct();
        product.setName(commonName);

        for (int i = 0; i < 5; i++) {
            var response = productClient.createProduct(product);
            assertEquals(response.getStatusCode(), 201);
        }

        var response = productClient.createProduct(product);
        TestUtils.assertKnownIssueOrExpected(response, 429, "BUG-QA2-07");
    }

    @Test(description = "Цены не могут содержать одинаковые цифры")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-QA2-08")
    public void testPriceWithRepeatingDigitsIsRejected() {
        Product product = seeder.generateProduct();
        product.setPrice(111.11);

        var response = productClient.createProduct(product);
        TestUtils.assertKnownIssueOrExpected(response, 400, "BUG-QA2-08");
    }
}
