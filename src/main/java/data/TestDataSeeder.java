package data;

import com.github.javafaker.Faker;
import domain.model.Product;
import domain.model.User;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import client.ProductClient;
import client.UserClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TestDataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(TestDataSeeder.class);

    private final ProductClient productClient = new ProductClient();
    private final UserClient userClient = new UserClient();
    private final Faker faker = new Faker(Locale.ENGLISH);

    private final List<Long> createdProductIds = new ArrayList<>();
    private final List<Long> createdUserIds = new ArrayList<>();

    public void seedUsers(int count) {
        logger.info("Создание {} фейковых пользователей через javafaker", count);
        for (int i = 0; i < count; i++) {
            User user = new User(
                    faker.name().fullName(),
                    faker.internet().emailAddress(),
                    faker.internet().password(6, 10)
            );
            Response response = userClient.createUser(user);
            long id = response.jsonPath().getLong("id");
            createdUserIds.add(id);

            logger.info("Создан пользователь: {} ({}), ID = {}", user.getName(), user.getEmail(), id);
        }
    }

    public void seedProducts() {
        logger.info("Генерация продуктов с учётом требований QA");

        Product[] products = {
                new Product("CheapProduct", 49.0,null),
                new Product("Exact50", 50.0, null),
                new Product("Above50", 51.0, null),
                new Product("DivBy3", 60.0, null),
                new Product("PrimePrice", 89.0, null),
                new Product("PalindromeProduct", 70.0, null),
                new Product("SpecialDelProduct", 90.0, null),
                new Product("DayProduct", 70.0, null)
        };


        for (Product product : products) {
            Response response = productClient.createProduct(product);
            long id = response.jsonPath().getLong("id");
            createdProductIds.add(id);
            logger.info("Создан продукт: {} — ID = {}", product.getName(), id);
        }
    }

    public List<Long> getCreatedProductIds() {
        return createdProductIds;
    }

    public List<Long> getCreatedUserIds() {
        return createdUserIds;
    }

    public void seedAll() {
        seedUsers(5);
        seedProducts();
    }
    public void clearAll() {
        // Очистить локальные списки с ID
        createdProductIds.clear();
        createdUserIds.clear();

        // При необходимости — удалить все данные из тестируемой системы через API,
        // например вызвать delete всех продуктов и пользователей

        // Вот пример (если нужно полностью очистить):
        // productClient.deleteAllProducts();
        // userClient.deleteAllUsers();

        // Либо добавить логи
        logger.info("Очистка всех сидированных данных выполнена");
    }

    // Получить количество продуктов на сервере
    public int getProductsCount() {
        return productClient.getAllProducts().size();
    }


}

