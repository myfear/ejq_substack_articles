package com.example.rest;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class MemeResourceTest {

    @Test
    void create_and_get() {
        String payload = """
                {
                  "title": "Keyboard Warrior",
                  "image_url": "https://example.com/kw.gif",
                  "tags": "keyboard,warrior"
                }
                """;
        String id = given()
                .contentType(ContentType.JSON)
                .body(payload)
                .when().post("/memes")
                .then()
                .statusCode(201)
                .body("title", equalTo("Keyboard Warrior"))
                .body("imageUrl", equalTo("https://example.com/kw.gif"))
                .extract().jsonPath().getString("id");

        given()
                .when().get("/memes/" + id)
                .then()
                .statusCode(200)
                .body("id", equalTo(id))
                .body("tags", contains("keyboard", "warrior"));
    }

    @Test
    void validation_error() {
        String bad = """
                {"title":"", "img":"", "rating": 10}
                """;
        given()
                .contentType(ContentType.JSON)
                .body(bad)
                .when().post("/memes")
                .then()
                .statusCode(400)
                .body("error", equalTo("validation"))
                .body("violations.size()", greaterThan(0));
    }
}