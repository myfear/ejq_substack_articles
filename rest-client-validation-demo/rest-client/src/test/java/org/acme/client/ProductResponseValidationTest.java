package org.acme.client;

import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class ProductResponseValidationTest {

    @Test
    void shouldFailWhenRemoteProductIsInvalid() {
        RestAssured.when()
                .get("/validated-products")
                .then()
                .statusCode(500)
                .body(containsString("ConstraintViolationException"));
    }
}