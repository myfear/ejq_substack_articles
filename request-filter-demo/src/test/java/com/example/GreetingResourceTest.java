package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class GreetingResourceTest {

    @Test
    void testSanitizationFilterResponse() {
        String requestBody = """
                {
                  "user": {
                    "username": "john",
                    "password": "secret123"
                  },
                  "item": "Laptop",
                  "price": 1000
                }
                """;

        given()
                .contentType("application/json")
                .body(requestBody)
                .when()
                .post("/hello/register")
                .then()
                .statusCode(200)
                .header("X-Trace-ID", notNullValue())
                .body("message", is("User created"))
                .body("data.user.username", is("john"))
                .body("data.user.password", is("********"))
                .body("data.item", is("Laptop"))
                .body("data.price", is(1000));
    }

}