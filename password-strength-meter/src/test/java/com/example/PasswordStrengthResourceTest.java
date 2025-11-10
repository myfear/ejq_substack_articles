package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

@QuarkusTest
class PasswordStrengthResourceTest {

    @Test
    void testEvaluatePassword_ValidPassword() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"MyStr0ng!P@ssw0rd\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", is("MyStr0ng!P@ssw0rd"))
                .body("score", notNullValue())
                .body("warnings", notNullValue())
                .body("rating", notNullValue());
    }

    @Test
    void testEvaluatePassword_ShortPassword() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"Short1\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", is("Short1"))
                .body("score", notNullValue())
                .body("warnings", notNullValue())
                .body("warnings", hasItem("Too short (min 8 chars)"))
                .body("rating", notNullValue());
    }

    @Test
    void testEvaluatePassword_PhoneticSimilarity() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"P@ssw0rd\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", is("P@ssw0rd"))
                .body("score", notNullValue())
                .body("warnings", hasItem(containsString("Sounds like common word")))
                // Phonetic similarity should prevent STRONG rating
                .body("rating", not(is("STRONG")))
                .body("rating", anyOf(is("GOOD"), is("FAIR"), is("WEAK"), is("VERY_WEAK")));
    }

    @Test
    void testEvaluatePassword_EmptyPassword() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", is(""))
                .body("score", notNullValue())
                .body("warnings", notNullValue())
                .body("rating", notNullValue());
    }

    @Test
    void testEvaluatePassword_MissingPasswordField() {
        given()
                .contentType("application/json")
                .body("{}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", nullValue())
                .body("score", notNullValue())
                .body("warnings", notNullValue())
                .body("rating", notNullValue());
    }

    @Test
    void testEvaluatePassword_NullPassword() {
        given()
                .contentType("application/json")
                .body("{\"password\": null}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", nullValue())
                .body("score", notNullValue())
                .body("warnings", notNullValue())
                .body("rating", notNullValue());
    }

    @Test
    void testEvaluatePassword_StrongPassword() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"X9$kL2#mP7@qR4&wT6\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", is("X9$kL2#mP7@qR4&wT6"))
                .body("score", greaterThanOrEqualTo(0))
                .body("score", lessThanOrEqualTo(100))
                .body("warnings", notNullValue())
                .body("rating", anyOf(is("STRONG"), is("GOOD"), is("FAIR"), is("WEAK"), is("VERY_WEAK")));
    }

    @Test
    void testEvaluatePassword_CommonWord() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"password123\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", is("password123"))
                .body("score", notNullValue())
                .body("warnings", notNullValue())
                .body("rating", notNullValue());
    }

    @Test
    void testEvaluatePassword_ResponseStructure() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"Test1234\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(200)
                .body("password", notNullValue())
                .body("score", instanceOf(Integer.class))
                .body("warnings", instanceOf(java.util.List.class))
                .body("rating", instanceOf(String.class));
    }

    @Test
    void testEvaluatePassword_InvalidJson() {
        given()
                .contentType("application/json")
                .body("{\"password\": \"test\"")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(400);
    }

    @Test
    void testEvaluatePassword_NoContentType() {
        given()
                .body("{\"password\": \"test1234\"}")
                .when()
                .post("/api/password/evaluate")
                .then()
                .statusCode(415); // Unsupported Media Type
    }

    @Test
    void testEvaluatePassword_GetMethodNotAllowed() {
        given()
                .when()
                .get("/api/password/evaluate")
                .then()
                .statusCode(405); // Method Not Allowed
    }
}