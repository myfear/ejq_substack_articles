package com.example.user;

import static io.restassured.RestAssured.given;
import static net.javacrumbs.jsonunit.JsonMatchers.jsonEquals;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class UserResourceTest {

    @Test
    void create_and_get_json_leniently() {
        given().contentType(ContentType.JSON)
                .body(new UserCreate("Markus", "markus@jboss.org", "ROLE_USER"))
                .when().post("/users/42")
                .then().statusCode(201)
                .body(jsonEquals("""
                          { "id": 42, "name": "Markus", "email": "markus@jboss.org", "role": "ROLE_USER" }
                        """));
    }
}