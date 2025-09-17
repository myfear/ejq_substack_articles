package com.example.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class AdminResourceAuthTest {

    @Test
    void anonymous_is_unauthorized() {
        given().when().get("/admin").then().statusCode(401);
    }

    @Test
    void user_with_wrong_role_is_forbidden() {
        given().auth().preemptive().basic("user", "user123")
                .when().get("/admin")
                .then().statusCode(403);
    }

    @Test
    void admin_with_correct_role_can_access() {
        given().auth().preemptive().basic("admin", "admin123")
                .when().get("/admin")
                .then().statusCode(200)
                .body(equalTo("secret"));
    }
}
