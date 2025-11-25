package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class ShortenerResourceTest {
    
    @Test
    void testShortenEndpoint() {
        String originalUrl = "https://example.com";
        
        String key = given()
          .contentType("text/plain")
          .body(originalUrl)
          .when().post("/api/shorten")
          .then()
             .statusCode(200)
             .body(notNullValue())
             .extract().asString();
        
        // Verify the key is not empty
        assert key != null && !key.isEmpty();
    }

    @Test
    void testRedirectEndpoint() {
        String originalUrl = "https://example.com";
        
        // First create a short link
        String key = given()
          .contentType("text/plain")
          .body(originalUrl)
          .when().post("/api/shorten")
          .then()
             .statusCode(200)
             .extract().asString();
        
        // Then test the redirect
        given()
          .redirects().follow(false)
          .when().get("/api/" + key)
          .then()
             .statusCode(302)
             .header("Location", is(originalUrl));
    }

    @Test
    void testRedirectEndpointNotFound() {
        given()
          .when().get("/api/nonexistent")
          .then()
             .statusCode(404);
    }

}