package org.acme.security;

import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
class SecurityLdapTest {

    @Test
    void public_is_open() {
        RestAssured.given()
                .get("/api/public")
                .then()
                .statusCode(200)
                .body(equalTo("public"));
    }

    @Test
    void admin_requires_role() {
        RestAssured.given()
                .auth().basic("adminUser", "adminUserPassword")
                .get("/api/admin")
                .then()
                .statusCode(200)
                .body(equalTo("admin"));
    }

    @Test
    void user_me_requires_standardRole() {
        RestAssured.given()
                .auth().basic("standardUser", "standardUserPassword")
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .body(equalTo("standardUser"));
    }
}