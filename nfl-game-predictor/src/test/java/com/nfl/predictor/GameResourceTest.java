package com.nfl.predictor;

import static io.restassured.RestAssured.given;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class GameResourceTest {

    @Test
    public void testGetGamesEndpoint() {
        given()
          .when().get("/api/admin/games")
          .then()
             .statusCode(200);
    }

    @Test
    public void testGetTeamsEndpoint() {
        given()
          .when().get("/api/admin/teams")
          .then()
             .statusCode(200);
    }
}