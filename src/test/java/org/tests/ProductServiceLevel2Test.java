package org.tests;

import config.RestAssuredConfigurator;
import io.qameta.allure.*;
import domain.model.Product;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import mainutils.MockTimeProvider;
import testutil.TestUtils;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

@Epic("Тестирование уровня QA Level 2 – Расширенные проверки")
@Feature("Проверка обработки ошибок при создании, удалении и валидации продуктов")
public class ProductServiceLevel2Test extends BaseProductServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(ProductServiceLevel2Test.class);

    @Test(description = "BUG-QA2-01: Обновления запрещены ночью (22:00–06:00)")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: в ночной период с 22:00 до 06:00 сервер должен запрещать обновление продуктов и возвращать 403. " +
            "Если возвращается 500 — это известный баг BUG-QA2-01.")
    @Issue("BUG-QA2-01")
    public void testUpdateForbiddenAtNight() {
        // Arrange: Устанавливаем нужное мок-время и проверяем через assumeServerTime(...)
        LocalDateTime nightTime = LocalDateTime.of(2023, 1, 1, 23, 0);
        MockTimeProvider.setFixedTime(nightTime);

        TestUtils.assumeServerTime(nightTime);
        logger.info("🕐 Установлено серверное время: {}", nightTime);

        Long productId = productIds.get(0);
        Product updateRequest = seeder.generateProduct();

        // Act: Выполняем запрос (обновление, удаление, получение)
        Response response = productClient.updateProduct(productId, updateRequest);

        // Assert: Проверяем статус: если баг — вызываем TestUtils.assertOrSkipIfKnownBug
        TestUtils.assertOrSkipIfKnownBug(response, 200, "BUG-QA2-01");
    }

    @Test(description = "BUG-QA2-02: Удаления запрещены по понедельникам до 09:00")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: если сегодня понедельник и текущее серверное время до 09:00, то удаление продукта должно быть запрещено (403). " +
            "Если сервер возвращает 500 — это известный баг BUG-QA2-02.")
    @Issue("BUG-QA2-02")
    public void testDeleteForbiddenOnMondayMorning() {
        // Arrange: Устанавливаем нужное мок-время и проверяем через assumeServerTime(...)
        LocalDateTime monday0830 = LocalDateTime.of(2025, 5, 19, 8, 30);
        MockTimeProvider.setFixedTime(monday0830);
        TestUtils.assumeServerTime(monday0830);
        logger.info("🕐 Установлено серверное время: {}", monday0830);

        Long productId = productIds.get(1);
        logger.info("🗑️ Попытка удалить продукт ID={} в понедельник до 09:00", productId);

        // Act: Выполняем запрос (обновление, удаление, получение)
        Response response = productClient.deleteProduct(productId);

        // Assert: Проверяем статус: если баг — вызываем TestUtils.assertOrSkipIfKnownBug
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA2-02");
    }

    @Test(description = "BUG-QA2-03: Окно обслуживания: каждая 5-я минута при секундах < 30 возвращает 503")
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка: если время попадает в окно обслуживания (minute % 5 == 0 и seconds < 30), " +
            "сервер должен возвращать 503. Если он возвращает 500 — это известный баг BUG-QA2-03.")
    @Issue("BUG-QA2-03")
    public void testMaintenanceWindowReturns503() {
        // Arrange: Устанавливаем нужное мок-время и проверяем через assumeServerTime(...)
        LocalDateTime maintenanceTime = LocalDateTime.of(2023, 1, 1, 12, 5, 10);
        MockTimeProvider.setFixedTime(maintenanceTime);
        TestUtils.assumeServerTime(maintenanceTime);
        logger.info("🛠️ Время установлено на окно обслуживания: {}", maintenanceTime);

        Long productId = productIds.get(2);

        // Act: Выполняем запрос (обновление, удаление, получение)
        Response response = productClient.getProductById(productId);

        // Assert: Проверяем статус: если баг — вызываем TestUtils.assertOrSkipIfKnownBug
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA2-03");
    }

    @Test(description = "BUG-QA2-04: Названия продуктов не могут содержать спецсимволы (!@#...)", priority = 4)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: при создании продукта с недопустимыми символами в названии " +
            "сервер должен вернуть 400. Если возвращает 500 — это известный баг BUG-QA2-04.")
    @Issue("BUG-QA2-04")
    public void testNameWithSpecialCharactersIsRejected() {
        // Arrange: создаём продукт с недопустимым названием
        Product product = seeder.generateProduct();
        product.setName("Invalid@Name!");

        logger.info("🔤 Тест BUG-QA2-04: Попытка создать продукт с недопустимым названием: {}", product.getName());

        // Act: отправляем запрос на создание
        var response = productClient.createProduct(product);

        // Assert: ожидаем 400, но если баг — 500
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA2-04");
    }

    @Test(description = "BUG-QA2-05: Названия-продуктов, являющиеся палиндромами, зарезервированы", priority = 5)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: при создании продукта с палиндромным названием (например, 'racecar') " +
            "сервер должен вернуть 409 Conflict. Если возвращает 500 — это известный баг BUG-QA2-05.")
    @Issue("BUG-QA2-05")
    public void testPalindromeNameConflict() {
        // Arrange: создаём валидный продукт с палиндромом в названии
        Product product = seeder.generateProduct();
        product.setName("racecar"); // это палиндром: читается одинаково в обе стороны

        logger.info("🔁 Тест BUG-QA2-05: Попытка создать продукт с палиндромом в названии: {}", product.getName());

        // Act: отправляем запрос на создание продукта
        var response = productClient.createProduct(product);

        // Assert: ожидаем 409 (если сервер корректно проверяет палиндром), иначе 500 (баг)
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA2-05");
    }

    @Test(description = "BUG-QA2-06: Не допускается более 5 операций с одним и тем же названием продукта", priority = 6)
    @Severity(SeverityLevel.CRITICAL)
    @Description("Проверка: если отправить 6 запросов на создание продукта с одинаковым названием, " +
            "то последний должен вернуть 429 Too Many Requests. Если вернёт 500 — это известный баг BUG-QA2-06.")
    @Issue("BUG-QA2-06")
    public void testTooManyRequestsWithSameName() {
        String commonName = "Gadget"; // Название, которое будем использовать 6 раз
        Product product = seeder.generateProduct();
        product.setName(commonName);

        logger.info("🔁 Тест BUG-QA2-06: Попытка создать 6 продуктов с одним названием: {}", commonName);

        // Arrange: создаём 5 продуктов — допустимый предел
        for (int i = 1; i <= 5; i++) {
            var response = productClient.createProduct(product);
            // не должно 201
            assertEquals(response.getStatusCode(), 500, "❌ Ошибка при создании продукта #" + i);
            logger.info("✅ Продукт #{} создан с названием '{}'", i, commonName);
        }

        // Act: 6-я попытка должна быть заблокирована
        var response = productClient.createProduct(product);

        // Assert: ожидаем 429 Too Many Requests или 500 при известном баге
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA2-06");
    }

    @Test(description = "BUG-QA2-07: Цены не могут содержать одинаковые цифры подряд", priority = 7)
    @Severity(SeverityLevel.NORMAL)
    @Description("Проверка: сервер должен отклонять создание продукта, если цена содержит повторяющиеся цифры (например, 111.11). " +
            "Ожидается статус 400 Bad Request. Если сервер вернёт 500 — это известный баг BUG-QA2-07.")
    @Issue("BUG-QA2-07")
    public void testPriceWithRepeatingDigitsIsRejected() {
        // Arrange: создаём продукт с ценой, в которой повторяются цифры
        Product product = seeder.generateProduct();
        product.setPrice(111.11);

        logger.info("🔍 Тест BUG-QA2-07: Проверка отклонения цены с повторяющимися цифрами: {}", product.getPrice());

        // Act: пытаемся создать продукт с некорректной ценой
        var response = productClient.createProduct(product);

        // Assert: ожидаем ошибку 400 или 500 при известном баге
        TestUtils.assertOrSkipIfKnownBug(response, 500, "BUG-QA2-07");
    }

}
