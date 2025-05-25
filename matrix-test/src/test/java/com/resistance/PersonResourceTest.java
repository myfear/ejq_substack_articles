package com.resistance;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItems;

@QuarkusTest
public class PersonResourceTest {

    @Test
    public void testCreateAndList() {
        given()
                .contentType("application/json")
                .body("{\"name\":\"Trinity\"}")
                .when()
                .post("/people")
                .then()
                .statusCode(201);

        given()
                .contentType("application/json")
                .when().get("/people")
                .then()
                .statusCode(200)
                .body("name", hasItems("Neo", "Trinity"));
    }
}