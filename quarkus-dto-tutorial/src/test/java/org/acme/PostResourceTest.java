package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;

import org.acme.dto.CreatePostDto;
import org.acme.dto.PostDto;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.http.ContentType;

@QuarkusIntegrationTest
public class PostResourceTest {

    @Test
    public void testCreateAndGetPost() {
        CreatePostDto newPost = new CreatePostDto();
        newPost.title = "Test Title";
        newPost.content = "Test content.";
        newPost.authorEmail = "test@example.com";

        PostDto createdPost = given()
            .contentType(ContentType.JSON)
            .body(newPost)
            .when().post("/posts")
            .then()
            .statusCode(201)
            .extract().as(PostDto.class);

        // Verify created post
        assert(createdPost.id != null);
        assert(createdPost.title.equals(newPost.title));

        // Test GET by ID
        given()
            .when().get("/posts/" + createdPost.id)
            .then()
            .statusCode(200)
            .body("title", is(newPost.title))
            .body("authorEmail", is(newPost.authorEmail));
    }

    @Test
    public void testGetAllPosts() {
        given()
          .when().get("/posts")
          .then()
             .statusCode(200)
             .body("size()", greaterThanOrEqualTo(0)); // Check if it returns a list
    }
    // Add more tests for update, delete, validation etc.
}