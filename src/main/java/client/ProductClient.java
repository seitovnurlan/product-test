package client;

import config.RestAssuredConfigurator;
import domain.model.Product;
import io.qameta.allure.Allure;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

import static io.restassured.RestAssured.baseURI;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * REST-клиент для работы с сущностью Product.
 * Отвечает за инкапсуляцию всех вызовов к API /api/products.
 * Используется во всех тестах уровня QA1–QA3.
 */
public class ProductClient {

    public ProductClient() {
        RestAssuredConfigurator.configure("/api/products");
    }

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);
    /**
     * Универсальный метод создания продукта через API
     */
    @Step("Создание продукта: {product}")
    public Response createProduct(Product product) {

        logRequest("POST", "</>", product);
        String url = baseURI;
        Response response = given()
                .contentType(JSON)
                .body(product)
                .post()
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
        String url = baseURI;
        logRequest("GET", "</>", null);

        Response response = given()
                .accept(JSON)
                .get()
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Получение продуктов с пагинацией (Response) — page={page}, size={size}")
    public Response getAllProductsResponse(int page, int size) {
        String path = "/" + "?page=" + page + "&size=" + size;
        logRequest("GET", path, null);

        Response response = given()
                .accept(JSON)
                .get(path)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Получение всех продуктов (List<Product>)")
    public List<Product> getAllProducts() {
        return getAllProductsResponse()
                .then()
//                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("content", Product.class);
    }

    @Step("Получение всех ID продуктов")
    public List<Long> getAllProductIds() {
        logger.info("📥 Получение всех продуктов для анализа ID");

        Response response = given()
                .get()
                .thenReturn();

        logResponse(response);

        if (response.statusCode() != 200) {
            throw new IllegalStateException("Не удалось получить список продуктов: " + response.statusCode());
        }
        return response.jsonPath().getList("id", Long.class);
    }
//        return getAllProducts()
//                .stream()
//                .map(Product::getId)
//                .toList(); // или .collect(Collectors.toList()) для Java 8


    @Step("Получение продукта по ID: {id}")
    public Response getProductById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID не может быть null при удалении");
        }
        logRequest("GET", "/{id}", null);

        Response response = given()
                .pathParam("id", id)
                .when()
                .accept(JSON)
                .get("/{id}")
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление продукта по ID: {id}")
    public Response deleteProduct(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("Product ID не может быть null при удалении");
        }
//        String path = "/" + "/" + id;
        logRequest("DELETE (by ID)", "/{id}", null);

        Response response = given()
                .pathParam("id", id)
                .when()
                .delete("/{id}")
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Массовое удаление продуктов (bulk): {ids}")
    public Response deleteProducts(List<Long> ids) {
        logRequest("DELETE (bulk)", "/", ids);

        Response response = given()
                .contentType(JSON)
                .body(ids)
                .when()
                .request("DELETE", "") // REST Assured требует ручной вызов метода, если передаётся тело для DELETE
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление всех продуктов")
    public Response deleteAllProducts() {
        logRequest("DELETE (all)", "/", null);

        Response response = given()
                .delete()
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Обновление продукта по ID: {id}")
    public Response updateProduct(Long id, Product product) {
        // 💥 Валидация входных данных
        if (id == null) {
            throw new IllegalArgumentException("Product ID не может быть null при обновлении");
        }
        if (product == null) {
            throw new IllegalArgumentException("Объект Product не может быть null");
        }
        String endpoint = "/" + id; // просто "/1", т.к. basePath уже установлен

        logRequest("PUT", endpoint, product);

        Response response = given()
                .contentType(JSON)
                .body(product)
                .when()
                .put("/{id}", id)
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
        int statusCode = response.getStatusCode();
        String responseBody = response.getBody() != null ? response.getBody().asPrettyString() : "";

        logger.info("⬅️ Код ответа: {}", statusCode);

        if (!responseBody.isBlank()) {
            logger.info("📭 Тело ответа: {}", responseBody);
        }

//        if (statusCode == 500) {
//            // Логируем как известный баг, не как ошибку
//            logger.warn(" Известный баг: сервер вернул 500 — возможно проблема в бизнес-валидации. Тело ответа:\n{}", responseBody);
//
//            // Если используете Allure, можно добавить вложение:
//            Allure.addAttachment("Known issue: Server returned 500", responseBody);
//
//            // Можно здесь не кидать исключение, чтобы тест не падал, а просто логировать баг
//        }
    }



    public void deleteAllProductsIndividually() {
        List<Product> all = getAllProducts();
        for (Product product : all) {
            deleteProduct(product.getId());
        }
    }
    public Product getProductByIdSafe(Long id) {
        Response response = getProductById(id);

        int status = response.getStatusCode();
        if (status != 200) {
            logger.error("❌ Ошибка при получении продукта ID {}: статус {}, тело: {}",
                    id, status, response.getBody().asString());
            throw new RuntimeException("Не удалось получить продукт по ID " + id +
                    ". Код: " + status);
        }

        return response.as(Product.class);
    }

}
