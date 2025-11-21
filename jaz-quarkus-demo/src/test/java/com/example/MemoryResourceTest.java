package com.example;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@QuarkusTest
class MemoryResourceTest {
    @Test
    void testMemoryEndpoint() {
        given()
          .when().get("/memory")
          .then()
             .statusCode(200)
             .body("status", notNullValue())
             .body("jvmMaxHeapMB", notNullValue())
             .body("jvmMaxHeapMB", instanceOf(Integer.class))
             .body("jvmUsedHeapMB", notNullValue())
             .body("jvmUsedHeapMB", instanceOf(Integer.class))
             .body("hostContainerOS", notNullValue())
             .body("jvmArguments", notNullValue())
             .body("jvmArguments", instanceOf(java.util.List.class));
    }

}