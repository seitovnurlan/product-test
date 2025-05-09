package org.tests;

import io.qameta.allure.*;
import domain.model.Product;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import io.restassured.response.Response;


import java.util.concurrent.TimeUnit;

@Epic("Тестирование уровня QA Level 3")
@Feature("Проверка сложных условий и дополнительных ограничений")
public class QaLevel3Test {

    private static final Logger logger = LoggerFactory.getLogger(QaLevel3Test.class);
    private final ProductClient productClient = new ProductClient();

    // Искусственная пауза для имитации задержек
    private void artificialDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(150);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Задержка была прервана: {}", e.getMessage());
        }
    }

    @Test(description = "Ошибка: Название — палиндром")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-009")
    @Description("Проверка ошибки при создании продукта с палиндромом в названии")
    public void testPalindromeNameError() {
        Product product = new Product("АрозАупаланАлапуазорА", 123.0, 21);
        logger.info("Проверка палиндрома в названии: {}", product.getName());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка палиндрома");
        } catch (Exception e) {
            logger.error("Поймана ошибка палиндрома: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Palindrome name error");
        }
    }

    @Test(description = "Ошибка: Название слишком длинное")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-010")
    @Description("Проверка ошибки при слишком длинном названии")
    public void testTitleTooLongError() {
        Product product = new Product("ОченьДлинноеНазвание".repeat(10), 120.0, 22);
        logger.info("Проверка длины названия: {}", product.getName().length());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка превышения длины названия");
        } catch (Exception e) {
            logger.warn("Поймана ошибка длины: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Title too long");
        }
    }

    @Test(description = "Ошибка: Отрицательная цена")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-011")
    @Description("Проверка ошибки при отрицательной цене продукта")
    public void testNegativePriceError() {
        Product product = new Product("Отрицательная цена", -50.0, 23);
        logger.info("Проверка отрицательной цены: {}", product.getPrice());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка отрицательной цены");
        } catch (Exception e) {
            logger.warn("Ошибка: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Negative price error");
        }
    }

    @Test(description = "Ошибка: Название содержит запрещённые символы")
    @Severity(SeverityLevel.MINOR)
    @Issue("BUG-012")
    @Description("Проверка ошибки при наличии запрещённых символов в названии")
    public void testTitleWithForbiddenChars() {
        Product product = new Product("Тестовый@Продукт#Ошибка", 100.0, 24);
        logger.info("Проверка запрещённых символов в названии: {}", product.getName());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка по символам в названии");
        } catch (Exception e) {
            logger.error("Поймана ошибка символов: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Forbidden characters in title");
        }
    }

    @Test(description = "Ошибка: Наличие пробелов в начале и конце названия")
    @Severity(SeverityLevel.MINOR)
    @Issue("BUG-013")
    @Description("Проверка ошибки при пробелах в начале и/или конце названия")
    public void testTitleWithLeadingOrTrailingSpaces() {
        Product product = new Product("  Обрезать пробелы  ", 89.0, 25);
        logger.info("Проверка пробелов в названии: '{}'", product.getName());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка наличия пробелов в начале/конце");
        } catch (Exception e) {
            logger.warn("Ошибка пробелов: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Leading/trailing spaces in title");
        }
    }

    @Test(description = "Ошибка: Дублирование ID")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("BUG-014")
    @Description("Проверка ошибки при повторном использовании одного и того же ID")
    public void testDuplicateIdError() {
        Product product = new Product("Уникальный ID",77.0, 26);
        logger.info("Создание продукта с ID: {}", product.getId());

        artificialDelay();

        try {
            productClient.createProduct(product);
        } catch (Exception e) {
            Assertions.fail("Неожиданная ошибка при первом создании: " + e.getMessage());
        }

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка при повторном использовании ID");
        } catch (Exception e) {
            logger.warn("Ошибка дублирования ID: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Duplicate ID error");
        }
    }

    @Test(description = "Ошибка: Слишком маленькое название")
    @Severity(SeverityLevel.MINOR)
    @Issue("BUG-015")
    @Description("Проверка ошибки, если название слишком короткое")
    public void testTitleTooShortError() {
        Product product = new Product("A", 99.0, 27);
        logger.info("Проверка короткого названия: '{}'", product.getName());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка короткого названия");
        } catch (Exception e) {
            logger.warn("Ошибка короткого названия: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Title too short");
        }
    }
    @Test(description = "Ошибка: Массовое удаление запрещено")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("BUG-016")
    @Description("Проверка ошибки при массовом удалении продуктов")
    public void testBatchDeletionError() {
        Product[] products = {
                new Product("Удаление 1", 100.0, 30),
                new Product("Удаление 2", 110.0, 31)
        };

        logger.info("Попытка массового удаления {} продуктов", products.length);

        artificialDelay();

        try {
            // Используем deleteAllProducts — он отправляет запрос DELETE /products
            Response response = productClient.deleteAllProducts();

            // Предполагаем, что массовое удаление запрещено => статус не должен быть 200
            int statusCode = response.getStatusCode();
            logger.info("Ответ сервера при массовом удалении: {}", statusCode);

            Assertions.assertThat(statusCode)
                    .as("Ожидается ошибка 4xx или 5xx при массовом удалении")
                    .isGreaterThanOrEqualTo(400);

            Assertions.assertThat(response.getBody().asString())
                    .containsIgnoringCase("Batch deletion not allowed");

        } catch (Exception e) {
            logger.error("Ошибка при выполнении массового удаления: {}", e.getMessage());
            Assertions.fail("Ошибка при выполнении запроса массового удаления", e);
        }
    }


}
