package client;

import domain.model.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * REST-клиент для работы с пользователями через API
 */
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
    private static final String BASE_URI = "http://localhost:31494/users";

    @Step("Создание пользователя: {user}")
    public Response createUser(User user) {
        logger.info("POST-запрос для создания пользователя: {}", user);
        Response response = given()
                .contentType(JSON)
                .body(user)
                .post(BASE_URI)
                .thenReturn();
        logger.info("Ответ: {} - {}", response.getStatusCode(), response.getBody().asString());
        return response;
    }

    @Step("Получение всех пользователей")
    public Response getAllUsers() {
        return given().accept(JSON).get(BASE_URI).thenReturn();
    }
}
