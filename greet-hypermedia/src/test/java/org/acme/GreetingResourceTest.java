package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class GreetingResourceTest {

    @Test
    void linkHeadersPresent() {
        given()
                .when().get("/greeting/Ada")
                .then()
                .statusCode(200)
                .header("Link", containsString("rel=\"self\""))
                .header("Link", containsString("/greeting/Ada"))
                .body(containsString("Hello, Ada!"));
    }
}