package com.example.user;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.emptyOrNullString;
import static org.hamcrest.Matchers.not;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class HeadersTest {

    @Test
    void x_trace_id_header_is_set_on_every_response() {
        // We don't assert a specific status code on purpose.
        // The ContainerResponseFilter runs for success and error responses alike.
        given()
                .when().get("/users/1")
                .then()
                .header("X-Trace-Id", not(emptyOrNullString()));
    }
}
