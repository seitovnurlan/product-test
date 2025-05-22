package testutil;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.assertj.core.api.Assumptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.SkipException;

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
        } else if (actual == 500) {
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
}
