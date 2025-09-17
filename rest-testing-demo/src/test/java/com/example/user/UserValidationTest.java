package com.example.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class UserValidationTest {

    @Test
    void create_with_invalid_email_returns_problem_details() {
        given().contentType(ContentType.JSON)
                .body(new UserCreate("Markus", "not-an-email", "ROLE_USER"))
                .when().post("/users/99")
                .then().statusCode(400)
                .body("title", equalTo("Constraint Violation"))
                .body("violations[0].message", containsString("must be a well-formed email"));
    }
}
