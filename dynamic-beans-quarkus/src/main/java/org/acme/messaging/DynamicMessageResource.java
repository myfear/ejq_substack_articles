package org.acme.messaging;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/dynamic")
public class DynamicMessageResource {

    @Inject
    DynamicMessageService service;

    @GET
    public String send(
            @QueryParam("to") String to,
            @QueryParam("msg") String msg) {
        String recipient = to != null ? to : "markus@example.com";
        String text = msg != null ? msg : "Dynamic @Produces example!";
        return service.send(recipient, text);
    }
}