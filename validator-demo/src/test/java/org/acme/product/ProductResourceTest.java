package org.acme.product;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class ProductResourceTest {

    @Test
    void shouldRejectDiscountAboveLimit() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\":\"Widget\",\"discount\":50}")
                .when()
                .post("/products")
                .then()
                .statusCode(400)
                .body(containsString("Discount exceeds allowed limit"));
    }
}