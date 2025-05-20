package client;

import domain.model.Product;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * REST-клиент для работы с сущностью Product.
 * Отвечает за инкапсуляцию всех вызовов к API /api/products.
 * Используется во всех тестах уровня QA1–QA3.
 */
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    // Базовый URL можно переопределить через VM-параметры или .env переменные
    private static final String BASE_URI = System.getProperty("api.base.url", "http://localhost:31494/api/products");

    /**
     * Универсальный метод создания продукта через API
     */
    @Step("Создание продукта: {product}")
    public Response createProduct(Product product) {
        String url = BASE_URI;
        logRequest("POST", url, product);

        Response response = given()
                .contentType(JSON)
                .body(product)
                .post(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    /**
     * Массовое создание продуктов. Ошибки логируются, не останавливают выполнение.
     */
    @Step("Массовое создание продуктов")
    public void createProductBatch(Product[] products) {
        logger.info("⏳ Начинается массовое создание {} продуктов", products.length);
        for (Product product : products) {
            try {
                Response response = createProduct(product);
                logger.info("✅ Продукт создан: {}, код ответа: {}", product.getName(), response.getStatusCode());
            } catch (Exception e) {
                logger.error("❌ Ошибка при создании продукта {}: {}", product.getName(), e.getMessage());
            }
        }
    }

    @Step("Получение всех продуктов (Response)")
    public Response getAllProductsResponse() {
        String url = BASE_URI;
        logRequest("GET", url, null);

        Response response = given()
                .accept(JSON)
                .get(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Получение всех продуктов (List<Product>)")
    public List<Product> getAllProducts() {
        return getAllProductsResponse()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("content", Product.class);
    }

    @Step("Получение продукта по ID: {id}")
    public Response getProductById(Long id) {
        String url = BASE_URI + "/" + id;
        logRequest("GET", url, null);

        Response response = given()
                .accept(JSON)
                .get(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление продукта по ID: {id}")
    public Response deleteProduct(Long id) {
        String url = BASE_URI + "/" + id;
        logRequest("DELETE (by ID)", url, null);

        Response response = given()
                .delete(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Массовое удаление продуктов (bulk): {ids}")
    public Response deleteProducts(List<Long> ids) {
        String url = BASE_URI;
        logRequest("DELETE (bulk)", url, ids);

        Response response = given()
                .contentType(JSON)
                .body(ids)
                .when()
                .request("DELETE", url) // REST Assured требует ручной вызов метода, если передаётся тело для DELETE
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление всех продуктов")
    public Response deleteAllProducts() {
        String url = BASE_URI;
        logRequest("DELETE (all)", url, null);

        Response response = given()
                .delete(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Обновление продукта по ID: {id} -> {product}")
    public Response updateProduct(Long id, Product product) {
        String url = BASE_URI + "/" + id; // Втсавляем ID в путь
        logRequest("PUT", url, product);

        Response response = given()
                .contentType(JSON)
                .body(product)
                .put(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    // 🔽 Утилитные методы логирования
    private void logRequest(String method, String url, Object body) {
        logger.info("➡️ {} {}", method, url);
        if (body != null) {
            logger.info("📦 Тело запроса: {}", body);
        }
    }

    private void logResponse(Response response) {
        logger.info("⬅️ Код ответа: {}", response.getStatusCode());
        if (response.getBody() != null && !response.getBody().asString().isBlank()) {
            logger.info("📭 Тело ответа: {}", response.getBody().asPrettyString());
        }
    }
}
