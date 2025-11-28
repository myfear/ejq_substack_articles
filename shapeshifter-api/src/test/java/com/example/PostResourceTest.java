package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
class PostResourceTest {

    @Test
    void testListContentPublicView() {
        // Public view: Content (title, author), TextPost (body, wordCount), VideoPost
        // (videoUrl, durationSeconds)
        given()
                .when().get("/api/content")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].id", is(1))
                .body("[0].title", is("My Thoughts"))
                .body("[0].author", is("Alice"))
                .body("[0].body", is("Quarkus is fast."))
                .body("[0].wordCount", is(3))
                .body("[1].id", is(2))
                .body("[1].title", is("Funny Cat"))
                .body("[1].author", is("Bob"))
                .body("[1].videoUrl", is("http://vid.eo/cat"))
                .body("[1].durationSeconds", is(60));
    }

    @Test
    void testListContentSubscriberView() {
        // Subscriber view adds: Authenticated (views) + PremiumFeature (adRevenue,
        // bitrate, fileSize)
        given()
                .when().get("/api/content?role=subscriber")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[0].views", is(1000)) // Authenticated
                .body("[0].adRevenue", equalTo(50.0f)) // PremiumFeature - BigDecimal serialized as float
                .body("[1].views", is(50000)) // Authenticated
                .body("[1].bitrate", is("4k")) // PremiumFeature
                .body("[1].fileSize", is("3000 MB")) // PremiumFeature
                .body("[1].adRevenue", equalTo(2500.0f)); // PremiumFeature - BigDecimal serialized as float
    }

    @Test
    void testListContentAdminView() {
        // Admin view adds: AdminFeature (fsk) - only on VideoPost
        given()
                .when().get("/api/content?role=admin")
                .then()
                .statusCode(200)
                .body("size()", is(2))
                .body("[1].fsk", is("12")); // AdminFeature - only VideoPost has this
    }

    @Test
    void testGetContentNotFound() {
        // Test 404 for non-existent content
        given()
                .when().get("/api/content/999")
                .then()
                .statusCode(404)
                .body("error", is("Content not found"));
    }

}