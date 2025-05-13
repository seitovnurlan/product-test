package client;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.model.Product;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

import java.util.List;

/**
 * REST-клиент для работы с сущностью Product через API.
 * Инкапсулирует все взаимодействия с эндпоинтами, обеспечивает повторное использование.
 */
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    // ⚠️ Используется NodePort 31494 — его можно заменить при изменении манифеста или использовании port-forward
    private static final String BASE_URI = "http://localhost:31494/api/products";

    @Step("Создание продукта: {product}")
    public Response createProduct(Product product) {
        String url = BASE_URI;
        logger.info("POST → {}", url);
        logger.info("Тело запроса: {}", product);

        Response response = given()
                .contentType(JSON)
                .body(product)
                .post(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Массовое создание продуктов")
    public void createProductBatch(Product[] products) {
        logger.info("Начинаем массовое создание {} продуктов", products.length);

        for (Product product : products) {
            try {
                Response response = createProduct(product);
                logger.info("Продукт создан с ID: {}", product.getId());
            } catch (Exception e) {
                logger.error("❌ Ошибка при создании продукта с ID {}: {}", product.getId(), e.getMessage());
                throw new RuntimeException("Ошибка при массовом создании продуктов", e);
            }
        }
    }

    @Step("Получение всех продуктов")
    public Response getAllProductsResponse() {
        String url = BASE_URI;
        logger.info("GET → {}", url);

        Response response = given()
                .accept(JSON)
                .get(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Получение всех продуктов (в виде списка)")
    public List<Product> getAllProducts() {
        return getAllProductsResponse()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Product.class);
    }

    @Step("Получение продукта по ID: {id}")
    public Response getProductById(long id) {
        String url = BASE_URI + "/" + id;
        logger.info("GET → {}", url);

        Response response = given()
                .accept(JSON)
                .get(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление продукта по ID: {id}")
    public Response deleteProduct(long id) {
        String url = BASE_URI + "/" + id;
        logger.info("DELETE → {}", url);

        Response response = given()
                .delete(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Массовое удаление всех продуктов")
    public Response deleteAllProducts() {
        String url = BASE_URI;
        logger.info("DELETE (bulk) → {}", url);

        Response response = given()
                .delete(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Обновление продукта: {product}")
    public Response updateProduct(Product product) {
        String url = BASE_URI;
        logger.info("PUT → {}", url);
        logger.info("Тело запроса: {}", product);

        Response response = given()
                .contentType(JSON)
                .body(product)
                .put(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    /**
     * Утилитный метод логирования тела ответа
     */
    private void logResponse(Response response) {
        logger.info("Код ответа: {}", response.getStatusCode());
        logger.info("Тело ответа: {}", response.getBody().asPrettyString());
    }

//    public Product createProductWithId(int id) {
//        Product p = new Product("Test" + id, 10.0, id);
//        createProduct(p);
//        return p;
//    }

}
