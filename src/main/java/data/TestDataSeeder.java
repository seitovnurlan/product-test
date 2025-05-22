package data;

import client.ProductClient;
import client.UserClient;
import com.github.javafaker.Faker;
import domain.model.Product;
import domain.model.User;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
/**
 * Генератор и загрузчик тестовых данных в систему.
 * Используется в @BeforeClass перед тестами.
 */

public class TestDataSeeder {
    /**
     * Класс для генерации мок-данных (сидирования) пользователей и продуктов
     * для тестов QA Level 1–3.
     */
    private static final Logger logger = LoggerFactory.getLogger(TestDataSeeder.class);
    private final Faker faker = new Faker();

    private final UserClient userClient = new UserClient();
    private final ProductClient productClient = new ProductClient();

    private final List<User> createdUsers = new ArrayList<>();
    private final List<Product> createdProducts = new ArrayList<>();

    private final List<Long> createdUserIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();
    private static final List<Double> VALID_PRICES = List.of(
            123.45, 543.21, 987.12, 10.0, 25.45, 98.56, 456.78, 321.89, 654.32
    );

    /**
     * Сидирует и пользователей, и продукты.
     * Значения по умолчанию: 10 пользователей, 10 продуктов.
     */
    public void seedAll() {
        seedAll(10, 10);
    }
    /**
     * Сидирует указанное количество пользователей и продуктов.
     * @param userCount количество пользователей
     * @param productCount количество продуктов
     */
    public void seedAll(int userCount, int productCount) {
        logger.info("Сидирование {} пользователей и {} продуктов", userCount, productCount);
        seedUsers(userCount);
        seedProducts(productCount);
    }
    /**
     * Создание мок-юзеров через API и сбор их ID.
     */
    @Step("Создание пользователей через UserClient")
    public void seedUsers(int count) {
        logger.info("Попытка создать {} пользователей", count);
        for (int i = 0; i < count; i++) {
            User user = new User(
                    faker.name().fullName(),
                    faker.internet().emailAddress(),
                    faker.internet().password(8, 12)
            );
            Response response = userClient.createUser(user);
//            if (response.statusCode() >= 200 || response.statusCode() < 300) {
            if (response.statusCode() == 200 || response.statusCode() == 201) {
                User created = response.as(User.class);
                createdUsers.add(created);
                logger.info("✅ Создан пользователь: {}, статус {} должно было 201", created.getName(), response.statusCode());
            } else {
//                logger.warn("Ошибка при создании пользователя: статус {}, тело: {}", response.statusCode(), response.getBody().asString());
                logger.error("❌ Не удалось создать пользователя: {}, статус: {}, тело: {}",
                        user.getName(), response.statusCode(), response.getBody().asString());
            }
        }
        if (createdUsers.isEmpty()) {
            logger.warn("⚠️ Ни одного пользователя не было успешно создано.");
        }
    }
    /**
     * Создание мок-продуктов через API и сбор их ID.
     */
    @Step("Создание продуктов через ProductClient")
    public void seedProducts(int count) {
    logger.info("Попытка создать {}, продуктов", count);
        List<String> materials = List.of("Plastic", "Metal", "Wood", "Glass", "Leather", "Cotton");
        double price = generateValidPrice();
        for (int i = 0; i < count; i++) {
            String newProdName = String.format("SafeProduct%03d", i + 1);
            //                    faker.commerce().productName(),
            String material = materials.get(faker.random().nextInt(materials.size()));
            Product product = new Product(
                    newProdName,
                    material,
                   price
            );
            Response response = productClient.createProduct(product);
            logger.info("Тело запроса: " + product);
//            if (response.statusCode() >= 200 || response.statusCode() < 300) {
            if (response.statusCode() == 201 || response.statusCode() == 200) {
                Product created = response.as(Product.class);
                createdProducts.add(created);
                createdProductIds.add(created.getId());
                logger.info("✅ Создан продукт: {}, ID: {}, статус {}", product.getName(), created.getId(), response.statusCode());
            } else {
//                logger.warn("Ошибка при создании продукта: статус {}, тело {}", response.statusCode(), response.getBody().asString());
                logger.error("❌ Не удалось создать продукт: {}, статус: {}, тело: {}",
                        product.getName(), response.statusCode(), response.getBody().asString());
            }
        }
        if (createdProducts.isEmpty()) {
            logger.warn("⚠️ Ни одного продукта не было успешно создано.");
        }
    }
    private double generateValidPrice() {
        return VALID_PRICES.get(faker.random().nextInt(VALID_PRICES.size()));
    }

    public List<User> getCreatedUsers() {
        return new ArrayList<>(createdUsers);
    }

    public List<Product> getCreatedProducts() {
        return new ArrayList<>(createdProducts);
    }

    // Геттеры для доступа к созданным ID (для тестов)

    public List<Long> getCreatedUserIds() {
        return createdUserIds;
    }

    public List<Long> getCreatedProductIds() {
        return createdProductIds;
    }

    //Этот метод возвращает количество сгенерированных продуктов
    public int getProductsCount() {
        return createdProductIds != null ? createdProductIds.size() : 0;
    }
/**
 * Генерация одного продукта без сохранения.
 */
    public Product generateProduct() {
        return new Product(
                faker.commerce().productName(),
                faker.commerce().material(),
                faker.number().randomDouble(2, 10, 1000)
        );
    }
    /**
     * Очистка всех сохранённых мок-данных из памяти (не удаляет из БД).
     */
    public void clearSeededData() {
        createdUsers.clear();
        createdProducts.clear();
        createdUserIds.clear();
        createdProductIds.clear();
        logger.info("🧹 Мок-данные очищены из памяти");
    }
}
