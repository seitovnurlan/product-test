package org.tests;

import io.restassured.RestAssured;
import io.restassured.response.Response;

public class ProductClient {

    private final String baseUrl = "http://localhost:8188"; // Убедись, что это правильный базовый URL для твоего приложения

    // Пример метода для создания продукта
    public Response createProduct(String name, double price, int id) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .contentType("application/json")
                .body("{\"name\": \"" + name + "\", \"price\": " + price + ", \"id\": " + id + "}")
                .post("/api/products");
    }

    // Пример метода для удаления продукта по ID
    public Response deleteProduct(int id) {
        return RestAssured.given()
                .baseUri(baseUrl)
                .delete("/api/products/" + id);
    }

    // Метод для массового удаления продуктов (для теста)
    public Response deleteAllProducts() {
        return RestAssured.given()
                .baseUri(baseUrl)
                .delete("/api/products");
    }
}
