package org.tests;

import io.qameta.allure.*;
import domain.model.Product;
import org.assertj.core.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import utils.MockTimeProvider;
import org.tests.ProductClient;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Epic("Тестирование уровня QA Level 2")
@Feature("Проверка обработки ошибок при создании, удалении и валидации продуктов")
public class QaLevel2Test {

    private static final Logger logger = LoggerFactory.getLogger(QaLevel2Test.class);

    private final ProductClient productClient = new ProductClient();
    private final MockTimeProvider mockTimeProvider = new MockTimeProvider();

    // Искусственная пауза для имитации задержек в продакшене
    private void artificialDelay() {
        try {
            TimeUnit.MILLISECONDS.sleep(200); // Задержка 200 мс
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Задержка была прервана: {}", e.getMessage());
        }
    }

    private boolean isWeekend() {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        return today == DayOfWeek.SATURDAY || today == DayOfWeek.SUNDAY;
    }

    @Test(description = "Ошибка: Чётный ID")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-001")
    @Description("Проверка обработки ошибки при создании продукта с чётным ID")
    public void testEvenIdError() {
        if (isWeekend()) {
            logger.info("Пропущен тест на чётный ID — выходной день");
            return;
        }

        Product product = new Product("Чётный ID", 199.99, 2);
        logger.info("Проверка чётного ID: {}", product.getId());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка при чётном ID");
        } catch (Exception e) {
            logger.error("Ошибка при создании продукта: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Even ID error");
        }
    }

    @Test(description = "Ошибка: Цена делится на 3")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-002")
    @Description("Проверка обработки ошибки, когда цена продукта делится на 3")
    public void testPriceDivisibleBy3() {
        Product product = new Product("Цена кратна 3", 300.0, 5);
        logger.info("Проверка цены, делящейся на 3: {}", product.getPrice());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка при цене, делящейся на 3");
        } catch (Exception e) {
            logger.warn("Ожидаемая ошибка: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Price divisible by 3 error");
        }
    }

    @Test(description = "Ошибка: Простое число в ID")
    @Severity(SeverityLevel.MINOR)
    @Issue("BUG-003")
    @Description("Проверка обработки ошибки при ID, являющемся простым числом")
    public void testPrimeIdError() {
        Product product = new Product("Простое число ID", 450.0, 7);
        logger.info("Проверка простого ID: {}", product.getId());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка при простом ID");
        } catch (Exception e) {
            logger.warn("Поймана ошибка: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Prime number error");
        }
    }

    @Test(description = "Ошибка: Превышение лимита цены")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-004")
    @Description("Проверка обработки ошибки при превышении лимита цены")
    public void testPriceLimitExceeded() {
        Product product = new Product("Слишком дорогой", 9999.99, 10);
        logger.info("Проверка лимита цены: {}", product.getPrice());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка при превышении лимита цены");
        } catch (Exception e) {
            logger.error("Ошибка создания дорогого продукта: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Price limit exceeded");
        }
    }

    @Test(description = "Ошибка: Нарушение временного ограничения")
    @Severity(SeverityLevel.MINOR)
    @Issue("BUG-005")
    @Description("Проверка ошибки, связанной с временем выполнения")
    public void testTimeConstraintError() {
        mockTimeProvider.setCurrentTime("2025-01-01T00:00:00");
        Product product = new Product("Ночное создание", 150.0, 3);

        logger.info("Проверка временного ограничения: {}", mockTimeProvider.getCurrentTime());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка по временным условиям");
        } catch (Exception e) {
            logger.warn("Поймана временная ошибка: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Time constraint error");
        }
    }

    @Test(description = "Ошибка: Невозможность удаления")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("BUG-006")
    @Description("Проверка ошибки при удалении продукта")
    public void testProductDeletionError() {
        Product product = new Product("Удаляемый продукт", 100.0, 8);

        logger.info("Проверка удаления продукта ID {}", product.getId());

        artificialDelay();

        try {
            productClient.deleteProduct(product.getId());
            Assertions.fail("Ожидалась ошибка удаления");
        } catch (Exception e) {
            logger.warn("Ошибка удаления: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Product deletion error");
        }
    }

    @Test(description = "Ошибка: Цена ниже допустимой")
    @Severity(SeverityLevel.NORMAL)
    @Issue("BUG-007")
    @Description("Проверка ошибки, если цена продукта слишком низкая")
    public void testPriceBelowMinimum() {
        Product product = new Product("Слишком дёшево", 0.0, 12);

        logger.info("Проверка минимальной цены: {}", product.getPrice());

        artificialDelay();

        try {
            productClient.createProduct(product);
            Assertions.fail("Ожидалась ошибка при нулевой цене");
        } catch (Exception e) {
            logger.warn("Ошибка создания продукта: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Price below minimum limit");
        }
    }

    @Test(description = "Ошибка: Массовое создание продуктов")
    @Severity(SeverityLevel.BLOCKER)
    @Issue("BUG-008")
    @Description("Проверка ошибки при создании продуктов в пакете")
    public void testBatchCreationError() {
        Product[] batch = {
                new Product("Batch 1", 100.0, 14),
                new Product("Batch 2", 150.0, 15)
        };

        logger.info("Проверка пакетного создания продуктов: {} штук", batch.length);

        artificialDelay();

        try {
            productClient.createProductBatch(batch);
            Assertions.fail("Ожидалась ошибка при массовом создании");
        } catch (Exception e) {
            logger.error("Ошибка пакетного создания: {}", e.getMessage());
            Assertions.assertThat(e.getMessage()).contains("Batch creation error");
        }
    }
}
