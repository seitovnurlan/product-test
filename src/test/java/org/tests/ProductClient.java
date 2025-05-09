package org.tests;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import domain.model.Product;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * REST-клиент для работы с сущностью Product через API.
 * Инкапсулирует все взаимодействия с эндпоинтами, обеспечивает повторное использование.
 */
public class ProductClient {

    private static final Logger logger = LoggerFactory.getLogger(ProductClient.class);

    private static final String BASE_URI = "http://localhost:8188/products";

    @Step("Создание продукта: {product}")
    public Response createProduct(Product product) {
        logger.info("Отправка POST-запроса на создание продукта: {}", product);
        return given()
                .contentType(JSON)
                .body(product)
                .post(BASE_URI)
                .thenReturn();
    }

    @Step("Массовое создание продуктов")
    public void createProductBatch(Product[] products) {
        logger.info("Начинаем массовое создание продуктов: {}", products.length);

        for (Product product : products) {
            try {
                // Для каждого продукта вызываем метод createProduct
                Response response = createProduct(product);
                logger.info("Продукт успешно создан: {}", product.getId());
                // Можно добавить дополнительную логику, например, обработку ответа (response)
            } catch (Exception e) {
                logger.error("Ошибка при создании продукта с ID {}: {}", product.getId(), e.getMessage());
                // Логируем ошибку, если создание одного из продуктов не удалось
                throw new RuntimeException("Ошибка при массовом создании продуктов: " + e.getMessage(), e);
            }
        }
    }

    @Step("Получение всех продуктов")
    public Response getAllProductsResponse() {
        logger.info("Выполняется GET-запрос на получение всех продуктов");
        return given()
                .accept(JSON)
                .get(BASE_URI)
                .thenReturn();
    }

    @Step("Получение всех продуктов (в виде списка)")
    public java.util.List<Product> getAllProducts() {
        return getAllProductsResponse()
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList(".", Product.class);
    }

    @Step("Получение продукта по ID: {id}")
    public Response getProductById(long id) {
        logger.info("Запрос продукта по ID: {}", id);
        return given()
                .accept(JSON)
                .get(BASE_URI + "/" + id)
                .thenReturn();
    }

    @Step("Удаление продукта по ID: {id}")
    public Response deleteProduct(long id) {
        logger.info("Удаление продукта с ID: {}", id);
        return given()
                .delete(BASE_URI + "/" + id)
                .thenReturn();
    }

    @Step("Массовое удаление всех продуктов")
    public Response deleteAllProducts() {
        logger.info("Отправка DELETE-запроса на массовое удаление");
        return given()
                .delete(BASE_URI)
                .thenReturn();
    }

    @Step("Обновление продукта: {product}")
    public Response updateProduct(Product product) {
        logger.info("PUT-запрос на обновление продукта: {}", product);
        return given()
                .contentType(JSON)
                .body(product)
                .put(BASE_URI)
                .thenReturn();
    }
}
