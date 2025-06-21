package org.acme;

import java.util.stream.Collectors;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.HttpHeaders;

@Path("/hello")
public class HeaderResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response hello(@Context HttpHeaders headers) {
        String allHeaders = headers.getRequestHeaders().entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .collect(Collectors.joining("\n"));
        return Response.ok("Hello from RESTEasy Reactive!\n\n" + "Your request headers:\n" + allHeaders)
                .header("X-Custom-Header", "Hello from the server!")
                .build();
    }

    @GET
    @Path("/large-payload")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getLargePayload() {
        String largeJson = "{\"data\":[" +
                "{\"id\":1,\"name\":\"A very long name to make the payload larger\"},"
                        .repeat(1000)
                +
                "{\"id\":1001,\"name\":\"Final entry\"}" +
                "]}";
        return Response.ok(largeJson).build();
    }
}
