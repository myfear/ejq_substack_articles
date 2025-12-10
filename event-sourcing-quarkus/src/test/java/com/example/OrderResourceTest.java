package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class OrderResourceTest {
    
    @Test
    void testOrderFlow() {
        // 1. Place an Order
        String orderId = given()
                .contentType("application/json")
                .body("{\"customerEmail\": \"alice@example.com\"}")
                .when()
                .post("/orders")
                .then()
                .statusCode(201)
                .body("aggregateId", notNullValue())
                .extract()
                .path("aggregateId");
        
        // 2. Add an Item
        given()
                .contentType("application/json")
                .body("{\"productName\": \"Laptop\", \"quantity\": 1, \"price\": 1499.00}")
                .when()
                .post("/orders/" + orderId + "/items")
                .then()
                .statusCode(200)
                .body("aggregateId", equalTo(orderId));
        
        // 3. Ship the Order
        given()
                .contentType("application/json")
                .body("{\"trackingNumber\": \"TRACK-123\"}")
                .when()
                .post("/orders/" + orderId + "/ship")
                .then()
                .statusCode(200)
                .body("aggregateId", equalTo(orderId));
        
        // 4. Inspect State - Current state derived from events
        given()
                .when()
                .get("/orders/" + orderId)
                .then()
                .statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("customerEmail", equalTo("alice@example.com"))
                .body("status", equalTo("SHIPPED"))
                .body("items", notNullValue())
                .body("items.size()", equalTo(1))
                .body("items[0].productName", equalTo("Laptop"))
                .body("items[0].quantity", equalTo(1))
                .body("items[0].price", equalTo(1499.00f))
                .body("total", equalTo(1499.00f));
        
        // 5. Inspect Event stream
        given()
                .when()
                .get("/orders/" + orderId + "/events")
                .then()
                .statusCode(200)
                .body("size()", equalTo(3))
                .body("[0].orderId", equalTo(orderId))
                .body("[0].timestamp", notNullValue())
                .body("[1].orderId", equalTo(orderId))
                .body("[1].timestamp", notNullValue())
                .body("[2].orderId", equalTo(orderId))
                .body("[2].timestamp", notNullValue());
        
        // 6. Inspect Read model
        given()
                .when()
                .get("/orders/" + orderId + "/read-model")
                .then()
                .statusCode(200)
                .body("orderId", equalTo(orderId))
                .body("customerEmail", equalTo("alice@example.com"))
                .body("status", equalTo("SHIPPED"))
                .body("total", equalTo(1499.00f))
                .body("lastUpdated", notNullValue());
    }
}