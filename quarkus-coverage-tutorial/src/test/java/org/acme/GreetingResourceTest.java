package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GreetingResourceTest {

    @Test
    public void testHelloEndpointWithName() {
        given()
                .queryParam("name", "Dev")
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello, Dev!"));
    }

    @Test
    public void testHelloEndpointNoName() {
        given()
                .when().get("/hello")
                .then()
                .statusCode(200)
                .body(is("Hello, stranger!"));
    }

    // No test for /hello?name=admin
    // No test for /hello/goodbye endpoint yet
}