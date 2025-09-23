package org.acme.client;

import io.quarkiverse.cxf.annotation.CXFClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.soap.GreetingRequest;
import org.acme.soap.GreetingResponse;
import org.acme.soap.GreetingService;

@ApplicationScoped
public class GreetingClientBean {

    @Inject
    @CXFClient("greeting")
    GreetingService client;

    public String callGreeting(String name) {
        GreetingRequest req = new GreetingRequest(name);
        GreetingResponse resp = client.greet(req);
        return resp.getMessage();
    }
}
