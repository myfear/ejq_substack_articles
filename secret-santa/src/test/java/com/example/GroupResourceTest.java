package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GroupResourceTest {

    private static Long groupId;

    @Test
    @Order(1)
    void testCompleteSecretSantaWorkflow() {
        // Step 1: Register a user
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "email": "organizer@example.com",
                            "password": "santa123"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(201);

        // Step 2: Create a group
        String groupResponse = given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Team Secret Santa 2025"
                        }
                        """)
                .when()
                .post("/api/groups")
                .then()
                .statusCode(201)
                .body("name", equalTo("Team Secret Santa 2025"))
                .body("inviteCode", notNullValue())
                .body("id", notNullValue())
                .extract().asString();

        // Extract group ID from response
        groupId = Long.parseLong(
                io.restassured.path.json.JsonPath.from(groupResponse).getString("id"));

        // Step 3: Add first member (Alice)
        given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "participantName": "Alice",
                            "participantEmail": "alice@example.com",
                            "wishlist": "Lego, coffee, surprise"
                        }
                        """)
                .when()
                .post("/api/groups/" + groupId + "/members")
                .then()
                .statusCode(201)
                .body("participantName", equalTo("Alice"))
                .body("participantEmail", equalTo("alice@example.com"))
                .body("wishlist", equalTo("Lego, coffee, surprise"))
                .body("groupId", equalTo(groupId.intValue()))
                .body("groupName", equalTo("Team Secret Santa 2025"));

        // Step 4: Add second member (Bob)
        given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "participantName": "Bob",
                            "participantEmail": "bob@example.com",
                            "wishlist": "Books, tea"
                        }
                        """)
                .when()
                .post("/api/groups/" + groupId + "/members")
                .then()
                .statusCode(201)
                .body("participantName", equalTo("Bob"))
                .body("participantEmail", equalTo("bob@example.com"))
                .body("wishlist", equalTo("Books, tea"))
                .body("groupId", equalTo(groupId.intValue()))
                .body("groupName", equalTo("Team Secret Santa 2025"));

        // Step 5: Generate pairings
        given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .when()
                .post("/api/groups/" + groupId + "/pairings")
                .then()
                .statusCode(200)
                .body("size()", equalTo(2))
                .body("[0].groupId", equalTo(groupId.intValue()))
                .body("[0].groupName", equalTo("Team Secret Santa 2025"))
                .body("[0].giverName", notNullValue())
                .body("[0].receiverName", notNullValue())
                .body("[0].receiverWishlist", notNullValue())
                // Verify that no one is their own Secret Santa
                .body("[0].giverId", not(equalTo(io.restassured.path.json.JsonPath.from(
                        given()
                                .auth().preemptive().basic("organizer@example.com", "santa123")
                                .contentType(ContentType.JSON)
                                .when()
                                .post("/api/groups/" + groupId + "/pairings")
                                .then()
                                .extract().asString())
                        .getInt("[0].receiverId"))));
    }

    @Test
    @Order(2)
    void testRegisterDuplicateUser() {
        // Try to register the same user again
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "email": "organizer@example.com",
                            "password": "santa123"
                        }
                        """)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(3)
    void testCreateGroupWithoutAuthentication() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Unauthorized Group"
                        }
                        """)
                .when()
                .post("/api/groups")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(4)
    void testAddMemberToNonExistentGroup() {
        given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "participantName": "Charlie",
                            "participantEmail": "charlie@example.com",
                            "wishlist": "Games"
                        }
                        """)
                .when()
                .post("/api/groups/99999/members")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(5)
    void testGeneratePairingsWithInsufficientMembers() {
        // Create a new group with only one member
        String newGroupResponse = given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "name": "Small Group"
                        }
                        """)
                .when()
                .post("/api/groups")
                .then()
                .statusCode(201)
                .extract().asString();

        Long smallGroupId = Long.parseLong(
                io.restassured.path.json.JsonPath.from(newGroupResponse).getString("id"));

        // Add only one member
        given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .body("""
                        {
                            "participantName": "Solo",
                            "participantEmail": "solo@example.com",
                            "wishlist": "Friends"
                        }
                        """)
                .when()
                .post("/api/groups/" + smallGroupId + "/members")
                .then()
                .statusCode(201);

        // Try to generate pairings with insufficient members
        given()
                .auth().preemptive().basic("organizer@example.com", "santa123")
                .contentType(ContentType.JSON)
                .when()
                .post("/api/groups/" + smallGroupId + "/pairings")
                .then()
                .statusCode(400)
                .body(containsString("at least 2 members"));
    }
}

// Made with Bob
