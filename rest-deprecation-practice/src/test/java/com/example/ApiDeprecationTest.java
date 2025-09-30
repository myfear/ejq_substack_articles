package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ApiDeprecationTest {

    @Test
    void v1EndpointShouldReturnDeprecationHeaders() {
        given()
                .header("Content-Type", "application/json")
                .header("X-Client-Id", "test-client")
                .body("{\"product\":\"Book\",\"quantity\":1}")
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(200)
                .header("Deprecation", equalTo("true"))
                .header("Sunset", containsString("2026"))
                .body("product", equalTo("Book"))
                .body("quantity", equalTo(1));
    }

    @Test
    void v1EndpointShouldRejectInvalidRequests() {
        given()
                .header("Content-Type", "application/json")
                .header("X-Client-Id", "bad-client")
                .body("{\"product\":\"\",\"quantity\":0}") // invalid
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(400);
    }

    @Test
    void v2EndpointShouldAcceptValidRequests() {
        given()
                .header("Content-Type", "application/json")
                .body("{\"product\":\"Pen\",\"quantity\":3,\"customerId\":\"cust-42\"}")
                .when()
                .post("/api/v2/orders")
                .then()
                .statusCode(200)
                .body("status", equalTo("NEW"))
                .body("customerId", equalTo("cust-42"))
                .body("quantity", equalTo(3));
    }

    @Test
    void v2EndpointShouldRejectInvalidRequests() {
        given()
                .header("Content-Type", "application/json")
                .body("{\"product\":\"\",\"quantity\":-1,\"customerId\":\"\"}") // invalid
                .when()
                .post("/api/v2/orders")
                .then()
                .statusCode(400);
    }

    @Test
    void metricsShouldCountDeprecatedUsage() {
        // Call v1 endpoint
        given()
                .header("Content-Type", "application/json")
                .header("X-Client-Id", "metrics-client")
                .body("{\"product\":\"Book\",\"quantity\":1}")
                .when()
                .post("/api/v1/orders")
                .then()
                .statusCode(200);

        // Verify metric is incremented
        given()
                .when()
                .get("/q/metrics")
                .then()
                .statusCode(200)
                .body(containsString("deprecated_orders_v1_requests_total"));
    }
}