package org.acme.messaging;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@QuarkusTest
class NotificationResourceTest {

    @Test
    void notify_usesConfiguredMessageService() {
        given()
                .when().get("/notify?to=test@example.com&msg=hi")
                .then()
                .statusCode(200)
                .body(containsString("hi"));
    }
}