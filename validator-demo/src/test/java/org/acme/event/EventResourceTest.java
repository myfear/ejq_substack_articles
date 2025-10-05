package org.acme.event;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.containsString;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;

@QuarkusTest
public class EventResourceTest {

    // Helper method for building valid event JSON
    private String buildEventJson(String startDate, String endDate) {
        return String.format("""
                {
                    "start": "%s",
                    "end": "%s"
                }
                """, startDate, endDate);
    }

    // Helper method for building event JSON with null dates
    private String buildEventJsonWithNulls(String startDate, String endDate) {
        return String.format("""
                {
                    "start": %s,
                    "end": %s
                }
                """, startDate != null ? "\"" + startDate + "\"" : "null",
                endDate != null ? "\"" + endDate + "\"" : "null");
    }

    @Test
    public void testCreateValidEvent() {
        String validEventJson = buildEventJson("2024-01-15", "2024-01-20");

        given()
                .contentType(ContentType.JSON)
                .body(validEventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(200)
                .body("start", is("2024-01-15"))
                .body("end", is("2024-01-20"));
    }

    @Test
    public void testCreateEventWithSameStartAndEndDate() {
        String eventJson = buildEventJson("2024-01-15", "2024-01-15");

        given()
                .contentType(ContentType.JSON)
                .body(eventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(400); // Should fail validation as end date is not after start date
    }

    @Test
    public void testCreateEventWithEndDateBeforeStartDate() {
        String invalidEventJson = buildEventJson("2024-01-20", "2024-01-15");

        given()
                .contentType(ContentType.JSON)
                .body(invalidEventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(400)
                .body(containsString("End date must be after start date"));
    }

    @Test
    public void testCreateEventWithNullStartDate() {
        String eventJson = buildEventJsonWithNulls(null, "2024-01-20");

        given()
                .contentType(ContentType.JSON)
                .body(eventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(200) // Should be valid as validator allows null dates
                .body("start", is((String) null))
                .body("end", is("2024-01-20"));
    }

    @Test
    public void testCreateEventWithNullEndDate() {
        String eventJson = buildEventJsonWithNulls("2024-01-15", null);

        given()
                .contentType(ContentType.JSON)
                .body(eventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(200) // Should be valid as validator allows null dates
                .body("start", is("2024-01-15"))
                .body("end", is((String) null));
    }

    @Test
    public void testCreateEventWithBothNullDates() {
        String eventJson = buildEventJsonWithNulls(null, null);

        given()
                .contentType(ContentType.JSON)
                .body(eventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(200) // Should be valid as validator allows null dates
                .body("start", is((String) null))
                .body("end", is((String) null));
    }

    @Test
    public void testCreateEventWithInvalidDateFormat() {
        String invalidEventJson = buildEventJson("2024/01/15", "2024-01-20");

        given()
                .contentType(ContentType.JSON)
                .body(invalidEventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(400); // Should fail due to invalid date format
    }

    @Test
    public void testCreateEventWithEmptyBody() {
        given()
                .contentType(ContentType.JSON)
                .body("{}")
                .when()
                .post("/events")
                .then()
                .statusCode(200) // Should be valid as both dates will be null
                .body("start", is((String) null))
                .body("end", is((String) null));
    }

    @Test
    public void testCreateEventWithInvalidJson() {
        String invalidJson = "{ \"start\": \"2024-01-15\", \"end\": \"2024-01-20\"";

        given()
                .contentType(ContentType.JSON)
                .body(invalidJson)
                .when()
                .post("/events")
                .then()
                .statusCode(400); // Should fail due to invalid JSON
    }

    @Test
    public void testCreateEventWithMissingContentType() {
        String validEventJson = buildEventJson("2024-01-15", "2024-01-20");

        given()
                .body(validEventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(415); // Should fail due to missing content type
    }

    @Test
    public void testCreateEventWithWrongContentType() {
        String validEventJson = buildEventJson("2024-01-15", "2024-01-20");

        given()
                .contentType(ContentType.TEXT)
                .body(validEventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(415); // Should fail due to wrong content type
    }

    @Test
    public void testCreateEventWithFutureDates() {
        LocalDate futureStart = LocalDate.now().plusDays(30);
        LocalDate futureEnd = LocalDate.now().plusDays(45);
        String eventJson = buildEventJson(futureStart.toString(), futureEnd.toString());

        given()
                .contentType(ContentType.JSON)
                .body(eventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(200)
                .body("start", is(futureStart.toString()))
                .body("end", is(futureEnd.toString()));
    }

    @Test
    public void testCreateEventWithPastDates() {
        LocalDate pastStart = LocalDate.now().minusDays(30);
        LocalDate pastEnd = LocalDate.now().minusDays(15);
        String eventJson = buildEventJson(pastStart.toString(), pastEnd.toString());

        given()
                .contentType(ContentType.JSON)
                .body(eventJson)
                .when()
                .post("/events")
                .then()
                .statusCode(200)
                .body("start", is(pastStart.toString()))
                .body("end", is(pastEnd.toString()));
    }
}
