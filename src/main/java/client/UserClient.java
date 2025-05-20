package client;

import domain.model.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

/**
 * REST-клиент для управления пользователями.
 * Используется в тестах уровня QA1 и в сидировании данных.
 */
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
    private static final String BASE_URI = System.getProperty("api.base.url", "http://localhost:31494/api/users");

    @Step("Создание пользователя: {user}")
    public Response createUser(User user) {
        String url = BASE_URI;
        logRequest("POST", url, user);

        Response response = given()
                .contentType(JSON)
                .body(user)
                .post(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Массовое создание пользователей")
    public void createUserBatch(User[] users) {
        logger.info("⏳ Начинается массовое создание {} пользователей", users.length);
        for (User user : users) {
            try {
                Response response = createUser(user);
                logger.info("✅ Пользователь создан: {}, код ответа: {}", user.getEmail(), response.getStatusCode());
            } catch (Exception e) {
                logger.error("❌ Ошибка при создании пользователя {}: {}", user.getEmail(), e.getMessage());
            }
        }
    }

    @Step("Получение всех пользователей")
    public List<User> getAllUsers() {
        Response response = given()
                .accept(JSON)
                .get(BASE_URI)
                .thenReturn();

        logResponse(response);
        return response.jsonPath().getList(".", User.class);
    }

    @Step("Удаление пользователя по ID: {id}")
    public Response deleteUser(Long id) {
        String url = BASE_URI + "/" + id;
        logRequest("DELETE", url, null);

        Response response = given()
                .delete(url)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление всех пользователей")
    public Response deleteAllUsers() {
        logRequest("DELETE", BASE_URI, null);

        Response response = given()
                .delete(BASE_URI)
                .thenReturn();

        logResponse(response);
        return response;
    }

    private void logRequest(String method, String url, Object body) {
        logger.info("➡️ {} {}", method, url);
        if (body != null) {
            logger.info("📦 Тело запроса: {}", body);
        }
    }

    private void logResponse(Response response) {
        logger.info("⬅️ Код ответа: {}", response.getStatusCode());
        logger.info("📭 Тело ответа: {}", response.getBody().asPrettyString());
    }
}
