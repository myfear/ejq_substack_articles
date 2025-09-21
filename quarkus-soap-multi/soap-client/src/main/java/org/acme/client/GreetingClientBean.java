package org.acme.client;

import org.acme.soap.GreetingService;

import io.quarkiverse.cxf.annotation.CXFClient;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class GreetingClientBean {

    @CXFClient("greeting")
    GreetingService client;

    public String callGreeting(String name) {
        return client.greet(name);
    }
}