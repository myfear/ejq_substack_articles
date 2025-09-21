package org.acme.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/proxy")
public class GreetingProxyResource {

    @Inject
    GreetingClientBean greetingClient;

    @GET
    public String proxy(@QueryParam("name") String name) {
        return greetingClient.callGreeting(name);
    }
}