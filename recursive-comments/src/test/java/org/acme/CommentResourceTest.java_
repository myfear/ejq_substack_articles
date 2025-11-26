package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class CommentResourceTest {

    @Test
    void testAddRootComment() {
        given()
                .contentType("application/json")
                .body("{\"content\": \"This is a root comment\"}")
                .when()
                .post("/comments")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("parentId", nullValue())
                .body("threadRootId", notNullValue())
                .body("content", is("This is a root comment"))
                .body("createdAt", notNullValue());
    }

    @Test
    void testAddRootCommentAndReplies() {
        // Add root comment
        Long rootId = given()
                .contentType("application/json")
                .body("{\"content\": \"Root comment\"}")
                .when()
                .post("/comments")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        // Add first reply to root
        Long firstReplyId = given()
                .contentType("application/json")
                .body("{\"content\": \"First reply to root\"}")
                .when()
                .post("/comments/" + rootId + "/replies")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("parentId", is(rootId.intValue()))
                .body("threadRootId", is(rootId.intValue()))
                .body("content", is("First reply to root"))
                .extract()
                .jsonPath()
                .getLong("id");

        // Add second reply to root
        Long secondReplyId = given()
                .contentType("application/json")
                .body("{\"content\": \"Second reply to root\"}")
                .when()
                .post("/comments/" + rootId + "/replies")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("parentId", is(rootId.intValue()))
                .body("threadRootId", is(rootId.intValue()))
                .body("content", is("Second reply to root"))
                .extract()
                .jsonPath()
                .getLong("id");

        // Add reply to first reply
        Long nestedReplyId = given()
                .contentType("application/json")
                .body("{\"content\": \"Reply to first reply\"}")
                .when()
                .post("/comments/" + firstReplyId + "/replies")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("parentId", is(firstReplyId.intValue()))
                .body("threadRootId", is(rootId.intValue()))
                .body("content", is("Reply to first reply"))
                .extract()
                .jsonPath()
                .getLong("id");

        // Verify thread structure
        given()
                .when()
                .get("/comments/thread/" + rootId)
                .then()
                .statusCode(200)
                .body("id", is(rootId.intValue()))
                .body("parentId", nullValue())
                .body("level", is(0))
                .body("content", is("Root comment"))
                .body("replies", hasSize(2))
                .body("replies[0].id", is(firstReplyId.intValue()))
                .body("replies[0].parentId", is(rootId.intValue()))
                .body("replies[0].level", is(1))
                .body("replies[0].content", is("First reply to root"))
                .body("replies[0].replies", hasSize(1))
                .body("replies[0].replies[0].id", is(nestedReplyId.intValue()))
                .body("replies[0].replies[0].parentId", is(firstReplyId.intValue()))
                .body("replies[0].replies[0].level", is(2))
                .body("replies[0].replies[0].content", is("Reply to first reply"))
                .body("replies[1].id", is(secondReplyId.intValue()))
                .body("replies[1].parentId", is(rootId.intValue()))
                .body("replies[1].level", is(1))
                .body("replies[1].content", is("Second reply to root"))
                .body("replies[1].replies", empty());
    }

    @Test
    void testAddRootCommentWithEmptyContent() {
        given()
                .contentType("application/json")
                .body("{\"content\": \"\"}")
                .when()
                .post("/comments")
                .then()
                .statusCode(400);
    }

    @Test
    void testAddReplyWithEmptyContent() {
        // First create a root comment
        Long rootId = given()
                .contentType("application/json")
                .body("{\"content\": \"Root comment\"}")
                .when()
                .post("/comments")
                .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        // Try to add reply with empty content
        given()
                .contentType("application/json")
                .body("{\"content\": \"\"}")
                .when()
                .post("/comments/" + rootId + "/replies")
                .then()
                .statusCode(400);
    }

    @Test
    void testAddReplyToNonExistentParent() {
        given()
                .contentType("application/json")
                .body("{\"content\": \"Reply to non-existent parent\"}")
                .when()
                .post("/comments/99999/replies")
                .then()
                .statusCode(500); // Service throws IllegalArgumentException which becomes 500
    }

    @Test
    void testGetThreadForNonExistentRoot() {
        given()
                .when()
                .get("/comments/thread/99999")
                .then()
                .statusCode(500); // Service throws IllegalArgumentException which becomes 500
    }
}