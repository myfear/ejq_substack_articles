package org.acme.messaging;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/notify")
public class NotificationResource {

    @Inject
    MessageService service;

    @GET
    public String notifyUser(@QueryParam("to") String to, @QueryParam("msg") String msg) {
        String recipient = to != null ? to : "markus@example.com";
        String text = msg != null ? msg : "Dynamic beans in Quarkus!";
        return service.send(recipient, text);
    }
}