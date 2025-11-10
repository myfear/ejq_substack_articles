package com.example;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class DeduplicationResourceTest {

    @TempDir
    Path tempDir;

    @Test
    void testFindDuplicates() throws IOException {
        // Create test files - some duplicates, some unique
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");
        Path file3 = tempDir.resolve("subdir/file3.txt");
        Path file4 = tempDir.resolve("unique.txt");

        // Create duplicate content (same hash)
        String duplicateContent = "This is duplicate content";
        Files.write(file1, duplicateContent.getBytes());
        Files.write(file2, duplicateContent.getBytes());
        Files.write(file3, duplicateContent.getBytes());

        // Create unique content
        Files.write(file4, "This is unique content".getBytes());

        // Call the endpoint
        given()
                .queryParam("path", tempDir.toAbsolutePath().toString())
                .when()
                .get("/deduplicate")
                .then()
                .statusCode(200)
                .body("scannedPath", notNullValue())
                .body("summary.duplicateGroups", greaterThan(0))
                .body("summary.totalDuplicateFiles", greaterThan(0))
                .body("duplicates", notNullValue());
    }

    @Test
    void testNoDuplicates() throws IOException {
        // Create unique files only
        Path file1 = tempDir.resolve("file1.txt");
        Path file2 = tempDir.resolve("file2.txt");

        Files.write(file1, "Unique content 1".getBytes());
        Files.write(file2, "Unique content 2".getBytes());

        given()
                .queryParam("path", tempDir.toAbsolutePath().toString())
                .when()
                .get("/deduplicate")
                .then()
                .statusCode(200)
                .body("message", is("No duplicate files found"));
    }

    @Test
    void testMissingPath() {
        given()
                .when()
                .get("/deduplicate")
                .then()
                .statusCode(400)
                .body("error", is("Path parameter is required"));
    }

    @Test
    void testNonExistentDirectory() {
        given()
                .queryParam("path", "/nonexistent/directory/path")
                .when()
                .get("/deduplicate")
                .then()
                .statusCode(404)
                .body("error", containsString("Directory does not exist"));
    }
}

