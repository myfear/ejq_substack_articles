package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class GreetingResourceTest {
    @Test
    @Disabled("N/A")
    void testHelloEndpoint() {
        given()
          .when().get("/feed")
          .then()
             .statusCode(200)
             .body(is("Hello from Quarkus REST"));
    }

}