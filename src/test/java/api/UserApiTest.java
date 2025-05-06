package api;

import io.qameta.allure.*;
import org.json.JSONObject;
import org.testng.annotations.Test;
import utils.MockDataGenerator;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@Epic("User API")
@Feature("User CRUD operations")

public class UserApiTest {

    private final Faker faker = new Faker();
    private int userId;
    private String name;
    private String email;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "http://localhost:31494";
    }

    @Test(priority = 1)
    @Story("Create new user")
    @Severity(SeverityLevel.CRITICAL)
    @Owner("your-name")
    @Description("Создание нового пользователя с валидными данными, ожидается 200 OK")
    public void createUser_shouldReturn200() {
        name = faker.name().fullName();
        email = faker.internet().emailAddress();
        String password = faker.internet().password();

        String body = """
            {
                "name": "%s",
                "email": "%s",
                "password": "%s"
            }
        """.formatted(name, email, password);

        Response response = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .body("name", equalTo(name))
                .body("email", equalTo(email))
                .extract().response();

        userId = response.path("id"); // сохраняем ID пользователя
    }

    @Test(priority = 2, dependsOnMethods = "createUser_shouldReturn200")
    @Story("Get user by ID")
    @Severity(SeverityLevel.NORMAL)
    @Owner("your-name")
    @Description("Получение созданного пользователя по ID, ожидается 200 OK")
    public void getUserById_shouldReturn200() {
        given()
                .pathParam("id", userId)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(200)
                .body("name", equalTo(name))
                .body("email", equalTo(email));
    }

    @Test(priority = 3, dependsOnMethods = "createUser_shouldReturn200")
    @Story("Update existing user")
    @Severity(SeverityLevel.NORMAL)
    @Owner("your-name")
    @Description("Обновление данных пользователя, ожидается 200 OK")
    public void updateUser_shouldReturn200() {
        String updatedName = faker.name().firstName();
        String updatedEmail = faker.internet().emailAddress();

        String body = """
            {
                "name": "%s",
                "email": "%s",
                "password": "updatedPass"
            }
        """.formatted(updatedName, updatedEmail);

        given()
                .contentType(ContentType.JSON)
                .pathParam("id", userId)
                .body(body)
                .when()
                .put("/api/users/{id}")
                .then()
                .statusCode(200)
                .body("name", equalTo(updatedName))
                .body("email", equalTo(updatedEmail));
    }

    @Test(priority = 4, dependsOnMethods = "createUser_shouldReturn200")
    @Story("Delete user by ID")
    @Severity(SeverityLevel.NORMAL)
    @Owner("your-name")
    @Description("Удаление пользователя по ID, ожидается 200 OK")
    public void deleteUser_shouldReturn200() {
        given()
                .pathParam("id", userId)
                .when()
                .delete("/api/users/{id}")
                .then()
                .statusCode(200);
    }

    @Test(priority = 5, dependsOnMethods = "deleteUser_shouldReturn200")
    @Story("Check deleted user")
    @Severity(SeverityLevel.MINOR)
    @Owner("your-name")
    @Description("Проверка удаления пользователя, ожидается 404 Not Found")
    public void getDeletedUser_shouldReturn404() {
        given()
                .pathParam("id", userId)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(404);
    }
}
