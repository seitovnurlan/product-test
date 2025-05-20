package testutils;

import com.github.javafaker.Faker;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MockDataGenerator {

    private static final Faker faker = new Faker();

    public static JSONObject generateUser() {
        JSONObject user = new JSONObject();
        user.put("name", faker.name().fullName());
        user.put("email", faker.internet().emailAddress());
        user.put("phone", faker.phoneNumber().cellPhone());
        return user;
    }

    public static JSONObject generateProduct() {
        JSONObject product = new JSONObject();
        product.put("name", faker.commerce().productName());
        product.put("description", faker.lorem().sentence());
        product.put("price", faker.number().randomDouble(2, 10, 1000));
        return product;
    }

    public static List<JSONObject> generateUsers(int count) {
        List<JSONObject> users = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            users.add(generateUser());
        }
        return users;
    }

    public static List<JSONObject> generateProducts(int count) {
        List<JSONObject> products = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            products.add(generateProduct());
        }
        return products;
    }
}
