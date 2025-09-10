package org.acme.todo.consumer;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.V4Pact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import io.quarkus.test.junit.QuarkusTest;


@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "TodoProvider", port = "8085")
@PactDirectory("target/pacts")
@QuarkusTest 
class TodoConsumerPactTest {

    @Pact(provider = "TodoProvider", consumer = "TodoConsumer")
    V4Pact pact(PactDslWithProvider builder) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        
        return builder
                .uponReceiving("get todo by id")
                .path("/api/todos/1").method("GET")
                .willRespondWith()
                .status(200).headers(headers)
                .body("{\"id\":1,\"title\":\"write tests\",\"done\":false}")
                .toPact(V4Pact.class);
    }

    @Test
    void consumer_parses_response() {
        given()
            .baseUri("http://localhost:8085")
        .when()
            .get("/api/todos/1")
        .then()
            .statusCode(200)
            .body(containsString("\"title\""));
    }
}
