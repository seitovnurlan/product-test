package org.tests;

import io.restassured.RestAssured;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import client.ProductClient;
import data.TestDataSeeder;

import java.util.ArrayList;
import java.util.List;


public abstract class BaseTest {

    protected static List<Long> productIds = new ArrayList<>();
    protected static TestDataSeeder seeder = new TestDataSeeder();
    protected static ProductClient productClient = new ProductClient();
    protected static Logger logger = LoggerFactory.getLogger(BaseTest.class);

    protected static final String BASE_URL = "http://localhost:31494";

    @BeforeClass(alwaysRun = true)
    public void globalSetup() {

        RestAssured.baseURI = BASE_URL;
        seeder = new TestDataSeeder();
        productClient = new ProductClient();


//        logger.info("📦 Очистка ранее созданных данных");
//        UserCleanupService cleanerUser = new UserCleanupService();
//        ProductCleanupService cleanerProd = new ProductCleanupService();
//        cleanerUser.cleanUpAllUsers();    // Полностью очищает пользователей в системе.
//        cleanerProd.cleanUpAllProducts(); // Полная очистка с понижением цены

//        logger.info("Загрузка мок-данных перед тестами уровня 1");
//        seeder.seedAll();
//        productIds = seeder.getCreatedProductIds();

        if (productIds == null || productIds.isEmpty()) {
            logger.info("📦 Проверка и инициализация данных перед тестами");

            List<Long> existingIds = productClient.getAllProductIds();

            if (existingIds == null || existingIds.isEmpty()) {
                logger.warn("❌ Продукты не найдены. Запускаем сидер...");
                seeder.seedAll();
                productIds = seeder.getCreatedProductIds();
                logger.info("✅ Сгенерированы продукты: {}", productIds);
            } else {
                productIds = existingIds;
                logger.info("✅ Используем существующие продукты: {}", productIds);
            }

            logger.info("📦 Загружено {} продуктов. ID: {}", productIds.size(), productIds);
        } else {
            logger.info("📦 Продукты уже загружены ранее: {}", productIds);
        }
    }
}
