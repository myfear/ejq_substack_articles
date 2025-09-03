package org.acme.coffee;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class CoffeeResourceTest {

    @Test
    void shouldListCoffees() {
        given()
                .when().get("/coffee")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThan(0))
                .body("[0].name", notNullValue());
    }

    @Test
    void shouldRejectInvalidCoffee() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"name\": \"\"}")
                .when().post("/coffee")
                .then()
                .statusCode(400);
    }
}