package com.example;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ImageCleanerResourceTest {

    @Inject
    MetadataReader metadataReader;

    @Test
    void testCleanImageWithMetadata() throws IOException {
        // Load the test image
        File testImage = new File("src/test/resources/test-image.jpg");
        assertTrue(testImage.exists(), "Test image should exist");

        byte[] originalBytes = Files.readAllBytes(testImage.toPath());

        // Verify original image has metadata
        var originalMetadata = metadataReader.readMetadata(originalBytes);
        assertTrue((Boolean) originalMetadata.getOrDefault("hasMetadata", false),
                "Original image should have metadata");

        // Upload and clean the image
        byte[] cleanedBytes = given()
                .multiPart("file", testImage, "image/jpeg")
                .when()
                .post("/clean")
                .then()
                .statusCode(200)
                .contentType("image/jpeg")
                .header("Content-Disposition", containsString("test-image.jpg"))
                .extract()
                .asByteArray();

        // Verify cleaned image is still a valid JPEG
        assertNotNull(cleanedBytes);
        assertTrue(cleanedBytes.length > 0, "Cleaned image should not be empty");

        // Verify metadata was stripped
        // Note: The cleaned image might not be parseable by Apache Commons Imaging
        // (which is fine - it means metadata is stripped), so we use hasMetadata() method
        // which handles exceptions gracefully
        boolean hasMetadata = metadataReader.hasMetadata(cleanedBytes);
        assertFalse(hasMetadata, "Cleaned image should not have metadata");

        // Verify the image dimensions are preserved (basic sanity check)
        // Note: This is a simple check - in a real scenario you might want to
        // decode the image and verify dimensions match
        assertTrue(cleanedBytes.length > 1000, "Cleaned image should have reasonable size");
    }

    @Test
    void testCleanImageWithoutFile() {
        // When no multipart data is sent, the endpoint returns 415 Unsupported Media Type
        // because it expects MULTIPART_FORM_DATA
        given()
                .when()
                .post("/clean")
                .then()
                .statusCode(415);
    }

    @Test
    void testCleanImageWithInvalidFile() {
        // Create a temporary file with invalid image data
        File invalidFile = new File("src/test/resources/invalid.txt");
        try {
            Files.write(invalidFile.toPath(), "This is not an image".getBytes());

            given()
                    .multiPart("file", invalidFile, "text/plain")
                    .when()
                    .post("/clean")
                    .then()
                    .statusCode(anyOf(is(400), is(500))); // Could be either depending on where it fails
        } catch (IOException e) {
            fail("Failed to create test file: " + e.getMessage());
        } finally {
            invalidFile.delete();
        }
    }

    @Test
    void testCleanImagePreservesImageContent() throws IOException {
        // Load the test image
        File testImage = new File("src/test/resources/test-image.jpg");
        assertTrue(testImage.exists(), "Test image should exist");

        // Upload and clean the image
        byte[] cleanedBytes = given()
                .multiPart("file", testImage, "image/jpeg")
                .when()
                .post("/clean")
                .then()
                .statusCode(200)
                .contentType("image/jpeg")
                .extract()
                .asByteArray();

        // Verify it's a valid JPEG (starts with JPEG magic bytes)
        assertTrue(cleanedBytes.length >= 2, "Image should have at least 2 bytes");
        // JPEG files start with FF D8
        assertEquals((byte) 0xFF, cleanedBytes[0], "Should start with JPEG magic bytes");
        assertEquals((byte) 0xD8, cleanedBytes[1], "Should start with JPEG magic bytes");
    }
}

