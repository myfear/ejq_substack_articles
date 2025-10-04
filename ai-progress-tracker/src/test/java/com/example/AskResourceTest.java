package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

@QuarkusTest
class GreetingResourceTest {
    @Test
    void testAiEndpoint() {
        given()
          .when().get("/ai/ask/stream?q=test")
          .then()
             .statusCode(200)
             .body(containsString("step"));
    }

}