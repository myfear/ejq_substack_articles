package com.resistance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.transaction.Transactional;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.hasItem;

@QuarkusTest
@TestProfile(TestProfileCustomData.class)
public class PersonTestWithCustomData {

    @BeforeEach
    @Transactional
    void setup() {
        // empty the database before each test
        Person.deleteAll();
        // insert a single person with name "Morpheus"
        Person person = new Person();
        person.name = "Morpheus";
        person.persist();
    }

    @Test
    void testOnlyMorpheusPresent() {
        given()
                .when().get("/people")
                .then()
                .statusCode(200)
                .body("name", hasItem("Morpheus"));
    }
}
