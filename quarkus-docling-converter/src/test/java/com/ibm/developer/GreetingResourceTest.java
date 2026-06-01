package com.ibm.developer;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
@QuarkusTest
class GreetingResourceTest {

    @Test
    void testConvertEndpointRejectsMissingFile() {
        given()
            .contentType("multipart/form-data")
            .when().post("/convert")
            .then()
                .statusCode(400);
    }

}
