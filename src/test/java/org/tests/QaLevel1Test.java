package org.tests;

import io.qameta.allure.*;
import io.restassured.http.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("QA Level 1")
@Feature("Basic validation rules")
public class QaLevel1Test extends BaseTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QaLevel1Test.class);

    @Test(description = "Cannot update product with ID divisible by 3")
    @Severity(SeverityLevel.CRITICAL)
    public void testUpdateProductDivisibleByThree() {
        int productId = 9; // divisible by 3
        String body = "{ \"name\": \"Test\", \"price\": 150 }";

        LOGGER.info("Проверка запрета обновления продукта с ID, делящимся на 3: ID={}", productId);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/products/" + productId)
                .then()
                .statusCode(400)
                .body("message", containsString("ID not allowed"));
    }

    @Test(description = "Product with prime ID requires special access")
    @Severity(SeverityLevel.CRITICAL)
    public void testPrimeIdRequiresAccess() {
        int[] primeIds = {2, 3, 5, 7};
        for (int id : primeIds) {
            LOGGER.info("Проверка ограничения доступа к продукту с простым ID: {}", id);

            given()
                    .contentType(ContentType.JSON)
                    .when()
                    .get("/api/products/" + id)
                    .then()
                    .statusCode(403)
                    .body("message", containsString("Access denied"));
        }
    }

    @Test(description = "Price change must not exceed 500")
    @Severity(SeverityLevel.NORMAL)
    public void testPriceChangeOverLimit() {
        int productId = 10;
        String body = "{ \"name\": \"Updated Product\", \"price\": 1600 }"; // если исходная цена была < 1100

        LOGGER.info("Проверка запрета на изменение цены больше, чем на 500 единиц: ID={}, New Price={}", productId, 1600);

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .put("/api/products/" + productId)
                .then()
                .statusCode(400)
                .body("message", containsString("Price change too large"));
    }

    @Test(description = "Cannot delete products over $100")
    @Severity(SeverityLevel.CRITICAL)
    public void testCannotDeleteExpensiveProduct() {
        int productId = 20;

        LOGGER.info("Проверка запрета на удаление продукта стоимостью более $100: ID={}", productId);

        given()
                .contentType(ContentType.JSON)
                .when()
                .delete("/api/products/" + productId)
                .then()
                .statusCode(403)
                .body("message", containsString("Cannot delete"));
    }

    @Test(description = "Cannot create product with price over $1000 without approval")
    @Severity(SeverityLevel.NORMAL)
    public void testCannotCreateExpensiveProduct() {
        String body = "{ \"name\": \"Gold Watch\", \"price\": 1200 }";

        LOGGER.info("Проверка запрета на создание продукта без одобрения при цене > $1000");

        given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/products")
                .then()
                .statusCode(403)
                .body("message", containsString("Approval required"));
    }

    @Test(description = "All operations have at least 100ms delay")
    @Severity(SeverityLevel.MINOR)
    public void testOperationDelay() {
        int productId = 10;

        LOGGER.info("Проверка наличия искусственной задержки не менее 100мс при запросе продукта");

        long start = System.currentTimeMillis();

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/api/products/" + productId)
                .then()
                .statusCode(anyOf(is(200), is(403), is(404))); // зависит от ID

        long duration = System.currentTimeMillis() - start;

        LOGGER.info("Задержка составила {} мс", duration);

        assert duration >= 100 : "Expected at least 100ms delay, but was: " + duration;
    }
}
