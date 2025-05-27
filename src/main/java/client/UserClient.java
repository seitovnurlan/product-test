package client;

import domain.model.User;
import config.RestAssuredConfigurator;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import io.restassured.specification.RequestSpecification;
import static io.restassured.RestAssured.basePath;
import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.testng.Assert.assertEquals;

/**
 * REST-клиент для управления пользователями.
 * Используется в тестах уровня QA1 и в сидировании данных.
 */
public class UserClient {

    private static final Logger logger = LoggerFactory.getLogger(UserClient.class);
//    private final RequestSpecification spec;

    public UserClient() {
        // Настраиваем basePath для всех запросов этого клиента
        RestAssuredConfigurator.configure("/api/users");

        // Локальный spec на случай использования новых методов
//        this.spec = given().basePath("/api/users").contentType(JSON);
    }
    @Step("Создание пользователя: {user}")
    public Response createUser(User user) {
        logRequest("POST", user);

        Response response = given()
                .contentType("application/json")
                .body(user)
                .post()
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
        logRequest("GET", null);
        Response response = given()
                .accept(JSON)
                .get()
                .thenReturn();

        logResponse(response);
        assertEquals(response.getStatusCode(), 200, "Некорректный статус-код при получении пользователей");
        return response.jsonPath().getList(".", User.class);
    }

    @Step("Удаление пользователя по ID: {id}")
    public Response deleteUser(Long id) {
        String path = "/" + id;
        logRequest("DELETE", null);

        Response response = given()
                .delete(path)
                .thenReturn();

        logResponse(response);
        return response;
    }

    @Step("Удаление всех пользователей")
    public Response deleteAllUsers() {
        logRequest("DELETE", null);

        Response response = given()
                .delete()
                .thenReturn();

        logResponse(response);
        return response;
    }

    private void logRequest(String method, Object body) {
        String fullUrl = RestAssured.baseURI + RestAssured.basePath;
        logger.info("➡️ {} {}", method, fullUrl);
        if (body != null) {
            logger.info("📦 Тело запроса: {}", body);
        }
    }

    private void logResponse(Response response) {
        logger.info("⬅️ Код ответа: {}", response.getStatusCode());
        logger.info("📭 Тело ответа: {}", response.getBody().asPrettyString());
    }
}
