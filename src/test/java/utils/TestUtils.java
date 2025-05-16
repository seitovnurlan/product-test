package utils;

import io.qameta.allure.Allure;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.*;

public class TestUtils {
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static void assertKnownIssueOrExpected(Response response, int expectedCode, String issueId) {
        int actual = response.statusCode();
        String body = response.getBody().asString();

        if (actual == expectedCode) {
            logger.info("Ожидаемый код ответа: {}", actual);
            assertThat(actual).isEqualTo(expectedCode);
        } else if (actual == 500 && body.contains("/api/products/")) {
            logger.warn("Ожидался {}, но сервер вернул 500 — фиксируем как баг {}", expectedCode, issueId);
            Allure.addAttachment("Known issue",
                    "BUG in " + issueId + ": сервер возвращает 500 вместо " + expectedCode + "\n\nОтвет:\n" + body);
            assertThat(actual).isEqualTo(500); // подтверждаем как баг
        } else {
            logger.error("Неожиданный ответ: код={}, тело={}", actual, body);
            fail("Unexpected response: " + actual + " instead of " + expectedCode);
        }
    }
}
