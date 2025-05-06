package api;

import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

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
    public void deleteUser_shouldReturn200() {
        given()
                .pathParam("id", userId)
                .when()
                .delete("/api/users/{id}")
                .then()
                .statusCode(200);
    }

    @Test(priority = 5, dependsOnMethods = "deleteUser_shouldReturn200")
    public void getDeletedUser_shouldReturn404() {
        given()
                .pathParam("id", userId)
                .when()
                .get("/api/users/{id}")
                .then()
                .statusCode(404);
    }
}
