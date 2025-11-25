package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;

@QuarkusTest
class AgeResourceTest {
    @Test
    void testAgeEndpoint() {
        given()
          .queryParam("dob", "1990-01-01")
          .when().get("/age")
          .then()
             .statusCode(200)
             .body("inputDob", is("1990-01-01"))
             .body("timezone", is("Europe/Berlin"))
             .body("epochSecondsLived", notNullValue())
             .body("daysLived", notNullValue())
             .body("isoPeriod", notNullValue())
             .body("humanPeriod", notNullValue())
             .body("ageYearsFloor", notNullValue())
             .body("planets", notNullValue())
             .body("calendars", notNullValue())
             .body("worldTimes", notNullValue());
    }

    @Test
    void testAgeEndpointWithTimezone() {
        given()
          .queryParam("dob", "2000-06-15")
          .queryParam("tz", "America/New_York")
          .when().get("/age")
          .then()
             .statusCode(200)
             .body("inputDob", is("2000-06-15"))
             .body("timezone", is("America/New_York"));
    }

    @Test
    void testAgeEndpointMissingDob() {
        given()
          .when().get("/age")
          .then()
             .statusCode(400);
    }

    @Test
    void testAgeEndpointBlankDob() {
        given()
          .queryParam("dob", "")
          .when().get("/age")
          .then()
             .statusCode(400);
    }

    @Test
    void testAgeEndpointInvalidDate() {
        given()
          .queryParam("dob", "invalid-date")
          .when().get("/age")
          .then()
             .statusCode(500);
    }

    @Test
    void testAgeEndpointInvalidTimezone() {
        given()
          .queryParam("dob", "1990-01-01")
          .queryParam("tz", "Invalid/Timezone")
          .when().get("/age")
          .then()
             .statusCode(500);
    }
}