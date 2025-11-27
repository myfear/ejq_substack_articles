package com.example.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class PaymentResourceTest {

    @Test
    void v1_request_and_response() {
        given()
                .header("X-API-Version", "2023-10-16")
                .contentType("application/json")
                .body("{\"amount\": 12.34}")
                .when()
                .post("/payments")
                .then()
                .statusCode(200)
                // V1 response only has “amount”
                .body("amount", equalTo(12.34f))
                .body("$", aMapWithSize(1));
    }

    @Test
    void v2_request_and_response() {
        given()
                .header("X-API-Version", "2024-03-15")
                .contentType("application/json")
                .body("{\"amount\": 99.99, \"method\": \"CARD\"}")
                .when()
                .post("/payments")
                .then()
                .statusCode(200)
                .body("amount", equalTo(99.99f))
                .body("method", equalTo("CARD"));
    }

    @Test
    void v3_request_v2_shape_but_richer_response() {
        given()
                .header("X-API-Version", "2024-09-01")
                .contentType("application/json")
                .body("{\"amount\": 50.00, \"method\": \"SEPA\"}")
                .when()
                .post("/payments")
                .then()
                .statusCode(200)
                .body("id", notNullValue())
                .body("status", anyOf(equalTo("AUTHORIZED"), equalTo("PENDING")))
                .body("confirmationRequired", isA(Boolean.class));
    }

    @Test
    void default_version_when_header_missing() {
        given()
                .contentType("application/json")
                .body("{\"amount\": 10.00, \"method\": \"CARD\"}")
                .when()
                .post("/payments")
                .then()
                .statusCode(200)
                // Defaults to application.properties -> 2024-09-01 (V3 response)
                .body("id", notNullValue())
                .body("confirmationRequired", isA(Boolean.class));
    }
}