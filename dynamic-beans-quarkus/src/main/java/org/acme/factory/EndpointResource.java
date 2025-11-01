package org.acme.factory;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/endpoint")
public class EndpointResource {

    @Inject
    EndpointClientFactory factory;

    @GET
    public String call(@QueryParam("id") String id, @QueryParam("path") String path) {
        var client = factory.client(id != null ? id : "payments");
        return client == null ? "unknown id" : client.call(path != null ? path : "/status");
    }
}