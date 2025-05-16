package org.acme;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@QuarkusTest
public class ErrorPagesTest {

    @Test
    public void testCustom404PageHtml() {
        given()
                .accept(MediaType.TEXT_HTML) // Explicitly request HTML
                .when().get("/this/path/does/not/exist")
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(containsString("text/html")) // Check content type contains text/html
                .body(containsString("Page Not Found")) // Check for content from your 404.html
                .body(containsString("<code>/this/path/does/not/exist</code>"));
    }

    @Test
    public void testCustom404PageJson() {
        given()
                .accept(MediaType.APPLICATION_JSON) // Explicitly request JSON
                .when().get("/api/this/path/does/not/exist") // Use an API path
                .then()
                .statusCode(Response.Status.NOT_FOUND.getStatusCode())
                .contentType(containsString("application/json"))
                .body("message", containsString("Resource not found at path: /api/this/path/does/not/exist"))
                .body("statusCode", is(404));
    }

    @Test
    public void testCustomApplicationErrorJson() {
        given()
                .accept(MediaType.APPLICATION_JSON)
                .when().get("/custom-error")
                .then()
                .statusCode(Response.Status.BAD_REQUEST.getStatusCode()) // As defined in MyCustomApplicationExceptionMapper
                .contentType(containsString("application/json"))
                .body("message", containsString("This is a test of the custom application exception."))
                .body("exceptionType", is("MyCustomApplicationException"));
    }
}
