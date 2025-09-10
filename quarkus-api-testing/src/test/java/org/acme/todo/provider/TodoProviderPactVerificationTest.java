package org.acme.todo.provider;

import org.acme.todo.Todo;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;


import au.com.dius.pact.provider.junit5.HttpTestTarget;
import au.com.dius.pact.provider.junit5.PactVerificationContext;
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider;
import au.com.dius.pact.provider.junitsupport.Provider;
import au.com.dius.pact.provider.junitsupport.loader.PactFolder;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;

@Provider("TodoProvider")
@PactFolder("target/pacts")
@QuarkusTest
class TodoProviderPactTest {

    @ConfigProperty(name = "quarkus.http.test-port", defaultValue = "8081")
    int quarkusPort;

    @BeforeEach
    void setTarget(PactVerificationContext context) {
        context.setTarget(new HttpTestTarget("localhost", quarkusPort));
    }

    @BeforeEach
    @Transactional
    void seedData() {
        if (Todo.findById(1L) == null) {
            var t = new Todo();
            t.title = "write tests";
            t.done = false;
            t.persist();
        }
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider.class)
    void verify(PactVerificationContext context) {
        context.verifyInteraction();
    }
}
