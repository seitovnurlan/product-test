package org.tests;

import data.TestDataSeeder;
import io.qameta.allure.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import client.ProductClient;
import domain.model.Product;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import testutils.TestUtils;
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
        logger.info("Загрузка мок-данных перед тестами уровня 1");
        seeder.seedAll();
        productIds = seeder.getCreatedProductIds();
        logger.info("Создано {} продуктов. ID: {}", productIds.size(), productIds);
    }

    @Test(description = "Продукты с чётными ID недоступны для получения")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA1-01")
    public void testEvenIdIsUnavailable() {
        Long evenId = productIds.stream().filter(id -> id % 2 == 0).findFirst()
                .orElseThrow(() -> new RuntimeException("Не найден чётный ID среди созданных продуктов"));

        logger.info("Проверка недоступности продукта с чётным ID: {}", evenId);
        var response = productClient.getProductById(evenId);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-01");
    }

    @Test(description = "Нельзя обновить продукт с ID кратным 3")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA1-02")
    public void testUpdateForbiddenForIdDivisibleByThree() {
        Long id = productIds.stream().filter(i -> i % 3 == 0).findFirst()
                .orElseThrow(() -> new RuntimeException("Не найден ID кратный 3 среди созданных продуктов"));

        logger.info("Проверка запрета обновления продукта с ID кратным 3: {}", id);
        Product update = new Product("Updated name", 100);
        var response = productClient.updateProduct(id, update);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-02");
    }

    @Test(description = "Простые ID требуют доступа: 2, 3, 5, 7")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA1-03")
    public void testPrimeIdsAreRestricted() {
        int[] primeIds = {2, 3, 5, 7};

        for (int id : primeIds) {
            logger.info("Проверка доступа к продукту с простым ID: {}", id);
            var response = productClient.getProductById((long) id);
            TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-03");
        }
    }

    @Test(description = "Создание продукта с ценой выше 1000 запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA1-04")
    public void testCreateProductWithHighPriceIsForbidden() {
        Product expensive = new Product("Expensive Product", 1500);
        logger.info("Проверка создания продукта с завышенной ценой: {}", expensive);
        var response = productClient.createProduct(expensive);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-04");
    }

    @Test(description = "Удаление продукта с ценой > 100 запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA1-05")
    public void testDeleteProductWithPriceOver100IsForbidden() {
        Product p = new Product("Pricey", 150);
        int id = productClient.createProduct(p).jsonPath().getInt("id");
        logger.info("Проверка удаления продукта с ценой {}: id={}", p.getPrice(), id);
        var response = productClient.deleteProduct((long) id);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-05");
    }

    @Test(description = "Изменение цены более чем на $500 запрещено")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-QA1-06")
    public void testPriceChangeMoreThan500IsForbidden() {
        Product original = new Product("Base", 100);
        int id = productClient.createProduct(original).jsonPath().getInt("id");
        Product updated = new Product("Base", 650);
        logger.info("Проверка обновления цены с превышением лимита более 500: id={}, {} -> {}", id, original.getPrice(), updated.getPrice());
        var response = productClient.updateProduct((long) id, updated);
        TestUtils.assertKnownIssueOrExpected(response, 403, "BUG-QA1-06");
    }

    @Test(description = "Проверка задержки всех операций минимум 100 мс")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-QA1-07")
    public void testMinimumDelayOf100ms() {
        Long id = productIds.get(0);
        logger.info("Проверка минимальной задержки операций: GET /products/{}", id);

        long start = System.currentTimeMillis();
        var response = productClient.getProductById(id);
        long duration = System.currentTimeMillis() - start;

        logger.info("Время отклика: {} мс", duration);
        assertThat("Ожидаемая минимальная задержка 100 мс", duration, greaterThanOrEqualTo(100L));
        assertThat(response.getStatusCode(), is(200));
    }
}
