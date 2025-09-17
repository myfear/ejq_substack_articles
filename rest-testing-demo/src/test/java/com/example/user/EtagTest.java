package com.example.user;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class EtagTest {

    @Test
    void conditional_get_returns_304_when_etag_matches() {
        // First create a user
        given().contentType(ContentType.JSON)
                .body(new UserCreate("Test User", "test@example.com", "ROLE_USER"))
                .when().post("/users/11")
                .then().statusCode(201);

        // Get the ETag
        var etag = given().accept(ContentType.JSON)
                .when().get("/users/11/etag")
                .then().statusCode(200)
                .extract().header("ETag");

        // Test conditional GET with matching ETag
        given().accept(ContentType.JSON)
                .header("If-None-Match", etag)
                .when().get("/users/11/etag")
                .then().statusCode(304);
    }
}
