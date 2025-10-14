package org.acme;

import io.quarkus.test.junit.*;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@QuarkusTest
public class FeedResourceTest
{
  @Test
  void testLatestEndpoint() {
    given()
      .when().get("/feed")
      .then()
      .statusCode(200)
      .contentType("application/json")
      .body("size()", lessThanOrEqualTo(5));
  }

  @Test
  void testLatestWithLimit() {
    given()
      .queryParam("limit", 3)
      .when().get("/feed")
      .then()
      .statusCode(200)
      .contentType("application/json")
      .body("size()", lessThanOrEqualTo(3));
  }

  @Test
  void testResetEndpoint() {
    given()
      .when().get("/feed/reset")
      .then()
      .statusCode(200)
      .contentType("application/json")
      .body(containsString("Metrics reset"));
  }
}
