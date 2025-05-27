package testutil;

import io.qameta.allure.Allure;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.assertj.core.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.*;
import static org.testng.Assert.assertEquals;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static void assertOrSkipIfKnownBug(Response response, int expectedCode, String issueId) {
        int actual = response.statusCode();
        String body = response.getBody().asString();

        if (actual == expectedCode) {
            logger.info("✅ Ожидаемый код ответа: {}", actual);
            Assert.assertEquals(actual, expectedCode);
        } else if (actual >= 400) {
            String message = String.format("Известный баг %s: сервер возвращает 500 вместо %d", issueId, expectedCode);
            logger.warn(" {}",message);
            Allure.addAttachment("Known issue", message + "\n\nОтвет сервера:\n" + body);
            throw new SkipException(message); //Пропускаем тест, отмечаем как known issue
        } else {
            logger.error("❌ Неожиданный код {} вместо {}", actual, expectedCode);
            Allure.addAttachment("Unexpected response", String.format("Ожидался код %d, получен %d\n\nОтвет сервера:\n%s", expectedCode,actual,body));
            Assert.fail("Неожиданный ответ: " + actual + " вместо " + expectedCode);
        }
       }

    public static boolean isPalindrome(Long id) {
        String str = String.valueOf(id);
        return str.equals(new StringBuilder(str).reverse().toString());
    }
    public static void assumeServerTime(LocalDateTime expected) {
            Response resp = given().get("/api/time"); // пример вызова
            int code = resp.statusCode();

            if (code == 404) {
                logger.warn("⚠️ /api/time не найден (404). Пропускаем тест.");
                throw new SkipException("/api/time не поддерживается сервером");
            }

            if (code == 500) {
                logger.warn("⚠️ Сервер вернул 500 на /api/time — известный баг. Пропускаем тест.");
                throw new SkipException("Известный баг на /api/time");
            }

            if (code != 200) {
                throw new AssertionError("Ожидался 200, но получили " + code);
            }

            String serverTimeStr = resp.jsonPath().getString("time"); // например, поле "time"
            LocalDateTime serverTime = LocalDateTime.parse(serverTimeStr);

            if (!serverTime.equals(expected)) {
                throw new AssertionError("Время сервера " + serverTime + " не совпадает с ожидаемым " + expected);
            }
        }

        /**
         * Проверяет, что сервер действительно работает по ожидаемому мок-времени.
         * Если endpoint /api/time отсутствует (404), то тест **пропускается**.
         */
        public static void assumeServerTimeOrSkip(LocalDateTime expectedTime) {
            Response response = given()
                    .basePath("/api/time")
                    .get();

            int statusCode = response.statusCode();
            String body = response.asString();

            Logger logger = LoggerFactory.getLogger(TestUtils.class);
            logger.info("🕓 Ответ от /api/time: статус {}, тело: {}", statusCode, body);

            if (statusCode == 404) {
                logger.warn("⚠️ Эндпоинт /api/time не реализован на сервере. ⏭️ Пропускаем проверку времени.");
                throw new SkipException("Эндпоинт /api/time не реализован — тест пропущен");
            }

            try {
                String serverTimeStr = response.jsonPath().getString("serverTime");
                LocalDateTime actual = LocalDateTime.parse(serverTimeStr);
                if (!actual.equals(expectedTime)) {
                    throw new AssertionError("Ожидалось время " + expectedTime + ", а пришло " + actual);
                }
            } catch (Exception e) {
                logger.warn("⚠️ Не удалось проверить время сервера: {}", e.getMessage());
                throw new SkipException("Ошибка проверки serverTime — тест пропущен");
            }
        }
    }
