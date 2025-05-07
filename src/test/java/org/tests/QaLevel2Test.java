package org.tests;

import io.qameta.allure.*;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.time.LocalTime;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@Epic("QA Level 2 — Расширенные проверки")
@Feature("Правила по времени, символам, дням недели и валидациям")
public class QaLevel2Test {

    private static final String BASE_URL = "http://localhost:31494/api/products";

    @Test(description = "Нельзя создать продукт в запрещённое время (00:00–06:00)")
    @Severity(SeverityLevel.CRITICAL)
    public void cannotCreateProductAtNight() {
        Response response = given()
                .queryParam("mockTime", "03:15")
                .contentType("application/json")
                .body(Map.of("id", 2001, "name", "NightProduct", "price", 99))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(anyOf(is(400), is(403))) // поведение зависит от реализации
                .extract().response();

        Allure.addAttachment("Response Body", response.asPrettyString());
    }

    @Test(description = "Можно создать продукт днём")
    @Severity(SeverityLevel.NORMAL)
    public void canCreateProductDuringDay() {
        given()
                .queryParam("mockTime", "14:00")
                .contentType("application/json")
                .body(Map.of("id", 2002, "name", "DayProduct", "price", 49))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(200)
                .body("name", equalTo("DayProduct"));
    }

    @Test(description = "Нельзя удалить продукт в понедельник")
    @Severity(SeverityLevel.CRITICAL)
    public void cannotDeleteProductOnMonday() {
        // Предварительное создание
        given()
                .queryParam("mockDay", "MONDAY")
                .contentType("application/json")
                .body(Map.of("id", 2003, "name", "TestProduct", "price", 50))
                .post(BASE_URL);

        Response response = given()
                .queryParam("mockDay", "MONDAY")
                .when()
                .delete(BASE_URL + "/2003")
                .then()
                .statusCode(anyOf(is(400), is(403)))
                .extract().response();

        Allure.addAttachment("Response Body", response.asPrettyString());
    }

    @Test(description = "Можно удалить продукт в другой день")
    @Severity(SeverityLevel.NORMAL)
    public void canDeleteProductOnTuesday() {
        // Предварительное создание
        given()
                .queryParam("mockDay", "TUESDAY")
                .contentType("application/json")
                .body(Map.of("id", 2004, "name", "SafeProduct", "price", 90))
                .post(BASE_URL);

        given()
                .queryParam("mockDay", "TUESDAY")
                .when()
                .delete(BASE_URL + "/2004")
                .then()
                .statusCode(200);
    }

    @Test(description = "Название с недопустимыми символами — ошибка")
    @Severity(SeverityLevel.NORMAL)
    public void invalidCharactersInName() {
        Response response = given()
                .contentType("application/json")
                .body(Map.of("id", 2005, "name", "Invalid#Name!", "price", 20))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(anyOf(is(400), is(422)))
                .extract().response();

        Allure.addAttachment("Response Body", response.asPrettyString());
    }

    @Test(description = "Название — палиндром: допустимо")
    @Severity(SeverityLevel.MINOR)
    public void palindromeNameAllowed() {
        given()
                .contentType("application/json")
                .body(Map.of("id", 2006, "name", "madam", "price", 60))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(200)
                .body("name", equalTo("madam"));
    }

    @Test(description = "Название — не палиндром: допустимо")
    @Severity(SeverityLevel.TRIVIAL)
    public void nonPalindromeStillAllowed() {
        given()
                .contentType("application/json")
                .body(Map.of("id", 2007, "name", "example", "price", 60))
                .when()
                .post(BASE_URL)
                .then()
                .statusCode(200);
    }
}
