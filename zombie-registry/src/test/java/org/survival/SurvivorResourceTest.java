package org.survival;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class SurvivorResourceTest {
    
    @Test
    void testGetAllSurvivors() {
        given()
          .when().get("/survivors")
          .then()
             .statusCode(200)
             .contentType("application/json");
    }

    @Test
    void testGetUnbittenSurvivors() {
        given()
          .when().get("/survivors/unbitten")
          .then()
             .statusCode(200)
             .contentType("application/json");
    }

    @Test
    void testGetLeaderboard() {
        given()
          .when().get("/survivors/leaderboard")
          .then()
             .statusCode(200)
             .contentType("application/json");
    }

    @Test
    void testGetLeaderboardWithLimit() {
        given()
          .queryParam("limit", 3)
          .when().get("/survivors/leaderboard")
          .then()
             .statusCode(200)
             .contentType("application/json");
    }

    @Test
    void testGetSurvivorStats() {
        given()
          .when().get("/survivors/stats")
          .then()
             .statusCode(200)
             .body("totalSurvivors", is(notNullValue()))
             .body("bitten", is(notNullValue()))
             .body("totalZombieKills", is(notNullValue()));
    }

    @Test
    void testRegisterNewSurvivor() {
        // Use a unique name to avoid conflicts with pre-loaded data
        String uniqueName = "Test Survivor " + System.currentTimeMillis();
        String survivorJson = String.format("""
            {
                "name": "%s",
                "zombieKills": 5,
                "skillSet": "WARRIOR",
                "daysSurvived": 10,
                "hasBeenBitten": false
            }
            """, uniqueName);
        
        given()
          .contentType("application/json")
          .body(survivorJson)
          .when().post("/survivors")
          .then()
             .statusCode(201)
             .body("name", is(uniqueName))
             .body("zombieKills", is(5))
             .body("skillSet", is("WARRIOR"))
             .body("daysSurvived", is(10))
             .body("hasBeenBitten", is(false))
             .body("id", is(notNullValue())); // Verify ID is auto-generated
    }

}