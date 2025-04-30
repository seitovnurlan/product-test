package base;

import com.codeborne.selenide.Configuration;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import static com.codeborne.selenide.Selenide.closeWebDriver;

public class BaseTest {

    @BeforeMethod
    public void setUp() {
        // Настройки Selenide
        Configuration.browser = "chrome";
        Configuration.browserSize = "1920x1080";
        Configuration.timeout = 10000;

        // Если нужен запуск в хедеcс-моде:
        // Configuration.headless = true;
    }

    @AfterMethod
    public void tearDown() {
        // Закрываем браузер после теста
        closeWebDriver();
    }
}
