package com.example.versioning;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class VersioningTest {

    @Test
    public void testPathVersioning() {
        given()
                .when().get("/v1/products")
                .then().statusCode(200)
                .body(containsString("description"));

        given()
                .when().get("/v2/products")
                .then().statusCode(200)
                .body(containsString("inStock"));
    }

    @Test
    public void testHeaderVersioning() {
        given()
                .header("X-API-Version", "2")
                .when().get("/products-header")
                .then().statusCode(200)
                .body(containsString("inStock"));
    }

    @Test
    public void testContentNegotiation() {
        given()
                .accept("application/vnd.myapi.v2+json")
                .when().get("/products-media")
                .then().statusCode(200)
                .body(containsString("inStock"));
    }
}