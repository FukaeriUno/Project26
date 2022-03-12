import com.codeborne.selenide.Condition;
import com.codeborne.selenide.Configuration;
import com.github.javafaker.Faker;
import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.LogDetail;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;
import static io.restassured.RestAssured.given;

// спецификация нужна для того, чтобы переиспользовать настройки в разных запросах
public class AuthTest {
    static RequestSpecification requestSpec = new RequestSpecBuilder()
            .setBaseUri("http://localhost")
            .setPort(9999)
            .setAccept(ContentType.JSON)
            .setContentType(ContentType.JSON)
            .log(LogDetail.ALL)
            .build();

    static Faker faker = new Faker(new Locale("eng"));
    static User userActive = new User(faker.name().username(), faker.internet().password(), "active");
    static User userBlocked = new User(faker.name().username(), faker.internet().password(), "blocked");

    @BeforeAll
    static void setUpAll() {
        Gson gson = new Gson();
        String json1 = gson.toJson(userActive);
        String json2 = gson.toJson(userBlocked);
        given() // "дано"
                .spec(requestSpec) // указываем, какую спецификацию используем
                .body(json1) // передаём в теле объект, который будет преобразован в JSON
                .when() // "когда"
                .post("/api/system/users") // на какой путь, относительно BaseUri отправляем запрос
                .then() // "тогда ожидаем"
                .statusCode(200); // код 200 OK
        given() // "дано"
                .spec(requestSpec) // указываем, какую спецификацию используем
                .body(json2) // передаём в теле объект, который будет преобразован в JSON
                .when() // "когда"
                .post("/api/system/users") // на какой путь, относительно BaseUri отправляем запрос
                .then() // "тогда ожидаем"
                .statusCode(200); // код 200 OK
    }

    @Test
    void shouldBeAuthWithActiveUser() {
        Configuration.holdBrowserOpen = true;
        open("http://localhost:9999/");
        $("[data-test-id=login] input").setValue(userActive.getLogin());
        $("[data-test-id=password] input").setValue(userActive.getPassword());
        $(".button").click();
        $(".heading").shouldHave(Condition.text("Личный кабинет"));
    }
    @Test
    void shouldBeNotAuthWithBlockedUser() {
        Configuration.holdBrowserOpen = true;
        open("http://localhost:9999/");
        $("[data-test-id=login] input").setValue(userBlocked.getLogin());
        $("[data-test-id=password] input").setValue(userBlocked.getPassword());
        $(".button").click();
        $("[data-test-id=error-notification] .notification__content").shouldHave(Condition.text("Ошибка! Пользователь заблокирован"));
    }

    @Test
    void shouldBeNotAuthWithInvalidLogin() {
        Configuration.holdBrowserOpen = true;
        open("http://localhost:9999/");
        $("[data-test-id=login] input").setValue("Вова");
        $("[data-test-id=password] input").setValue("123Qwerty");
        $(".button").click();
        $("[data-test-id=error-notification] .notification__content").shouldHave(Condition.text("Ошибка! Неверно указан логин или пароль"));
    }

    @Test
    void shouldBeNotAuthWithInvalidPassword() {
        Configuration.holdBrowserOpen = true;
        open("http://localhost:9999/");
        $("[data-test-id=login] input").setValue("Vova");
        $("[data-test-id=password] input").setValue("Пароль");
        $(".button").click();
        $("[data-test-id=error-notification] .notification__content").shouldHave(Condition.text("Ошибка! Неверно указан логин или пароль"));
    }
}