package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class BookResourceTest {

    @Test
    void testGetBooks() {
        given()
                .when().get("/books")
                .then()
                .statusCode(200)
                .body("$", isA(java.util.List.class));
    }

    @Test
    void testPostBook() {
        String requestBody = """
                {
                  "isbn": "978-0-123456-78",
                  "coupons": [
                    {
                      "type": "amount",
                      "name": "Summer Sale",
                      "amount": 10.00
                    }
                  ]
                }
                """;

        given()
                .contentType("application/json")
                .body(requestBody)
                .when().post("/books")
                .then()
                .statusCode(200)
                .body("isbn", is("978-0-123456-78"))
                .body("coupons", isA(java.util.List.class))
                .body("coupons[0].type", is("amount"))
                .body("coupons[0].name", is("Summer Sale"))
                .body("coupons[0].amount", is(10.00f))
                .body("id", notNullValue());
    }
}