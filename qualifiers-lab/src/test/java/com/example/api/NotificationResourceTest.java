package com.example.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class NotificationResourceTest {

    @Test
    void email_should_use_email_service() {
        given()
                .contentType("application/json")
                .body("{\"message\":\"X\"}")
                .when()
                .post("/notify/email")
                .then()
                .statusCode(200)
                .body(is("EMAIL sent: X"));
    }

    @Test
    void sms_should_use_sms_service() {
        given()
                .contentType("application/json")
                .body("{\"message\":\"Y\"}")
                .when()
                .post("/notify/sms")
                .then()
                .statusCode(200)
                .body(is("SMS sent: Y"));
    }
}