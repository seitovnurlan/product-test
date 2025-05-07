package org.tests;

import io.qameta.allure.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.tests.ProductClient;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Epic("QA Level 3")
@Feature("Сложные сценарии и правила")
public class QaLevel3Test {

    private static final Logger LOGGER = LoggerFactory.getLogger(QaLevel3Test.class);
    private ProductClient productClient;

    @BeforeClass
    public void setUp() {
        productClient = new ProductClient();
    }

    @Test(description = "Массовое удаление разрешено при < 10 продуктах")
    @Severity(SeverityLevel.CRITICAL)
    @Issue("QA3-01")
    public void testBulkDeleteWhenLessThanTenProducts() {
        LOGGER.info("Создаём 3 продукта для массового удаления");
        for (int i = 1; i <= 3; i++) {
            productClient.createProduct("Product" + i, 10.0 * i, 3000 + i);
        }

        LOGGER.info("Проверяем, что массовое удаление срабатывает");
        Response response = productClient.deleteAllProducts();
        response.then().statusCode(anyOf(is(200), is(204)));
    }

    @Test(description = "Массовое удаление запрещено при >= 10 продуктах")
    @Severity(SeverityLevel.NORMAL)
    @Issue("QA3-02")
    public void testBulkDeleteFailsWhenTenOrMoreProducts() {
        LOGGER.info("Создаём 10 продуктов");
        for (int i = 0; i < 10; i++) {
            productClient.createProduct("BulkProd" + i, 10.0 + i, 4000 + i);
        }

        LOGGER.info("Пробуем массово удалить — ожидаем ошибку");
        Response response = productClient.deleteAllProducts();
        response.then().statusCode(400)
                .body("message", containsString("Cannot delete more than"));
    }

    @Test(description = "Проверка отклонения по времени (например, выходной)")
    @Severity(SeverityLevel.MINOR)
    @Issue("QA3-03")
    public void testTimeBasedRejection() {
        LOGGER.info("Имитация запроса в запрещённое время (нужно реализовать через mock времени, если доступно)");
        // Здесь должна быть реализация мок-времени, если доступна.
        LOGGER.warn("Пропущена проверка: требуется внедрение time mocking или query-параметр времени");
    }

    @Test(description = "Проверка логики по дню недели — воскресенье (например, скидки/ограничения)")
    @Severity(SeverityLevel.MINOR)
    @Issue("QA3-04")
    public void testWeekdayBasedLogic() {
        DayOfWeek today = LocalDateTime.now().getDayOfWeek();
        LOGGER.info("Сегодня день недели: {}", today);

        if (today == DayOfWeek.SUNDAY) {
            LOGGER.info("Воскресенье — проверяем, что продукт создать нельзя (пример)");
            Response response = productClient.createProduct("SundayProduct", 50.0, 9999);
            response.then().statusCode(400)
                    .body("message", containsString("not allowed on Sunday"));
        } else {
            LOGGER.info("Не воскресенье — продукт должен создаться успешно");
            Response response = productClient.createProduct("WeekProduct", 50.0, 9998);
            response.then().statusCode(201);
        }
    }
}
