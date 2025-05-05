package org.acme;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import static io.restassured.RestAssured.given;

import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class GraphQLResourceTest {

    @Test
    void testQueryUserWithOrdersAndSettings() {
        given()
          .contentType("application/json")
          .body("{\"query\":\"{ user(id: \\\"123\\\") { name email orders { id total } settings { theme } } }\"}")
          .when()
          .post("/graphql")
          .then()
          .statusCode(200)
          .body("data.user.name", equalTo("Alice"))
          .body("data.user.orders.size()", equalTo(2))
          .body("data.user.settings.theme", equalTo("dark"));
    }

    @Test
    void testMutationUpdateEmail() {
        given()
          .contentType("application/json")
          .body("{\"query\":\"mutation { updateUserEmail(id: \\\"123\\\", email: \\\"newalice@example.com\\\") { email } }\"}")
          .when()
          .post("/graphql")
          .then()
          .statusCode(200)
          .body("data.updateUserEmail.email", equalTo("newalice@example.com"));
    }
}
