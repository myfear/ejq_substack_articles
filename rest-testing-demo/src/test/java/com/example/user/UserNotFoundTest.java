package com.example.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class UserNotFoundTest {

    @Test
    void get_unknown_user_yields_problem_404() {
        given().accept(ContentType.JSON)
                .when().get("/users/404")
                .then().statusCode(404)
                .body("title", equalTo("Not Found"))
                .body("detail", containsString("not found"));
    }
}