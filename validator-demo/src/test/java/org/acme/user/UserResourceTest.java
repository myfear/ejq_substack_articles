package org.acme.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class UserResourceTest {

    @Test
    public void testValidUserRegistration() {
        User validUser = new User();
        validUser.setUsername("john_doe");
        validUser.setEmail("john.doe@example.com");
        validUser.setBirthDate(LocalDate.of(1990, 1, 15));

        given()
                .contentType(ContentType.JSON)
                .body(validUser)
                .when()
                .post("/users")
                .then()
                .statusCode(200)
                .body("username", is("john_doe"))
                .body("email", is("john.doe@example.com"))
                .body("birthDate", is("1990-01-15"));
    }

    @Test
    public void testInvalidUserRegistration_EmptyUsername() {
        User invalidUser = new User();
        invalidUser.setUsername("");
        invalidUser.setEmail("john.doe@example.com");
        invalidUser.setBirthDate(LocalDate.of(1990, 1, 15));

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Username is required"));
    }

    @Test
    public void testInvalidUserRegistration_InvalidEmail() {
        User invalidUser = new User();
        invalidUser.setUsername("john_doe");
        invalidUser.setEmail("not-an-email");
        invalidUser.setBirthDate(LocalDate.of(1990, 1, 15));

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Invalid email address"));
    }

    @Test
    public void testInvalidUserRegistration_FutureBirthDate() {
        User invalidUser = new User();
        invalidUser.setUsername("john_doe");
        invalidUser.setEmail("john.doe@example.com");
        invalidUser.setBirthDate(LocalDate.now().plusDays(1)); // Future date

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Birth date must be in the past"));
    }

    @Test
    public void testInvalidUserRegistration_NullBirthDate() {
        User invalidUser = new User();
        invalidUser.setUsername("john_doe");
        invalidUser.setEmail("john.doe@example.com");
        invalidUser.setBirthDate(null);

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Birth date is required"));
    }

    @Test
    public void testInvalidUserRegistration_MultipleValidationErrors() {
        User invalidUser = new User();
        invalidUser.setUsername(""); // Empty username
        invalidUser.setEmail("invalid-email"); // Invalid email
        invalidUser.setBirthDate(null); // Null birth date

        given()
                .contentType(ContentType.JSON)
                .body(invalidUser)
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Username is required"))
                .body(containsString("Invalid email address"))
                .body(containsString("Birth date is required"));
    }

    @Test
    void shouldRejectWeakPassword() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"username\":\"markus\",\"email\":\"markus@example.com\",\"password\":\"abc123\"}")
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Password must include"));
    }

    @Test
    void shouldRejectInvalidNestedCustomer() {
        given()
                .contentType(ContentType.JSON)
                .body("{\"customer\":{\"name\":\"\",\"email\":\"\"},\"address\":{\"street\":\"\",\"city\":\"\"}}")
                .when()
                .post("/users")
                .then()
                .statusCode(400)
                .body(containsString("Username is required"));
    }

}