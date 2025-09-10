package org.acme.todo;

import static io.restassured.RestAssured.given;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
class TodoResourceTest {

    @Test
    void create_and_get() {
        Long id = given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"write tests\"}")
                .when()
                .post("/api/todos")
                .then()
                .statusCode(201)
                .header("Location", Matchers.containsString("/api/todos/"))
                .extract().jsonPath().getLong("id");

        given()
                .when()
                .get("/api/todos/{id}", id)
                .then()
                .statusCode(200)
                .body("title", Matchers.equalTo("write tests"))
                .body("done", Matchers.equalTo(false));
    }

    @Test
    void validation_error_on_blank_title() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"title\":\"\"}")
                .when()
                .post("/api/todos")
                .then()
                .statusCode(400); // Bean Validation kicks in
    }
}