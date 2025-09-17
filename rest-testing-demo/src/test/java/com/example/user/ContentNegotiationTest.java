package com.example.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ContentNegotiationTest {

    @Test
    void plain_text_when_requested() {
        // First create a user
        given().contentType(ContentType.JSON)
                .body(new UserCreate("Markus", "markus@jboss.org", "ROLE_USER"))
                .when().post("/users/5")
                .then().statusCode(201);

        // Test content negotiation for plain text
        given().accept("text/plain")
                .when().get("/users/5")
                .then().statusCode(200)
                .contentType("text/plain")
                .body(containsString("Markus <markus@jboss.org> [ROLE_USER]"));
    }
}