package tests;

import base.BaseTest;
import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Selenide;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Owner;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static com.codeborne.selenide.Selenide.*;
import static com.codeborne.selenide.Condition.*;

public class LoginPageTest extends BaseTest {

    @Epic("Login")
    @Feature("Login Page")
    @Story("Отображение заголовка")
    @Owner("nurlan")
    @Test
    public void openLoginPageAndCheckTitle() {
        // Открываем страницу
        Selenide.open("https://app.talentlms.com/login");

        // Проверяем, что заголовок содержит "Login"
        $("h1").shouldHave(text("Log in to your online training"));
//        $("h1").shouldHave(Condition.matchText("(?i).*log in.*")); частичное совпадение

    }
}
