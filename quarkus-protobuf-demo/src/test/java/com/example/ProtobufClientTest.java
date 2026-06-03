package com.example;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import build.buf.validate.Violation;
import build.buf.validate.Violations;
import org.junit.jupiter.api.Test;

import com.example.proto.CreateUserRequest;
import com.example.proto.User;
import com.example.proto.UserList;

import io.quarkus.test.junit.QuarkusTest;

import java.util.Map;

@QuarkusTest
class ProtobufClientTest {

    @Test
    void testGetAllUsers() throws Exception {
        byte[] responseBody = given()
                .accept("application/x-protobuf")
                .when()
                .get("/api/users")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asByteArray();

        UserList userList = UserList.parseFrom(responseBody);

        System.out.println("Total users: " + userList.getTotalCount());
        for (User user : userList.getUsersList()) {
            System.out.printf("User: id=%d, name=%s, email=%s%n",
                    user.getId(), user.getName(), user.getEmail());
        }

        assertTrue(userList.getTotalCount() > 0);
    }

    @Test
    void testGetUserById() throws Exception {
        byte[] responseBody = given()
                .accept("application/x-protobuf")
                .when()
                .get("/api/users/1")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asByteArray();

        User user = User.parseFrom(responseBody);

        System.out.printf("User: id=%d, name=%s, email=%s%n",
                user.getId(), user.getName(), user.getEmail());

        assertEquals(1, user.getId());
        assertEquals("Alice", user.getName());
    }

    @Test
    void testCreateUser() throws Exception {
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setName("Charlie")
                .setEmail("charlie@example.com")
                .build();

        byte[] requestBody = request.toByteArray();

        byte[] responseBody = given()
                .contentType("application/x-protobuf")
                .accept("application/x-protobuf")
                .body(requestBody)
                .when()
                .post("/api/users")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .asByteArray();

        User newUser = User.parseFrom(responseBody);

        System.out.printf("Created user: id=%d, name=%s, email=%s%n",
                newUser.getId(), newUser.getName(), newUser.getEmail());

        assertEquals("Charlie", newUser.getName());
        assertEquals("charlie@example.com", newUser.getEmail());
        assertTrue(newUser.getId() > 0);
    }

    @Test
    void testInvalidUser() throws Exception {
        // Send an invalid user with no name and a bad email address.
        CreateUserRequest request = CreateUserRequest.newBuilder()
                .setEmail("charlie_example.com")
                .build();

        byte[] requestBody = request.toByteArray();

        byte[] responseBody = given()
                .contentType("application/x-protobuf")
                .accept("application/x-protobuf")
                .body(requestBody)
                .when()
                .post("/api/users")
                .then()
                .statusCode(400)
                .extract()
                .body()
                .asByteArray();

        // Build map of expectations and our violations Protobuf message.
        Map<String, String> expectedViolations = Map.of(
                "name", "value length must be at least 1 characters",
                "email", "value must be a valid email address"
        );
        Violations violations = Violations.parseFrom(responseBody);


        // Make sure we have exactly the expected errors.
        assertEquals(expectedViolations.size(), violations.getViolationsCount(),
                "Expected " + expectedViolations.size() + " violations");
        expectedViolations.forEach((fieldName, expectedMessage) -> {
            Violation violation = violations.getViolationsList().stream()
                    .filter(v -> v.getField().getElements(0).getFieldName().equals(fieldName))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Expected violation for '" + fieldName + "' field"));

            assertEquals(expectedMessage, violation.getMessage(),
                    "Unexpected message for field '" + fieldName + "'");
        });
    }

}

