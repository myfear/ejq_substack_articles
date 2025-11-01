package org.acme.data;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/data")
public class DataResource {

    @Inject
    DataService service;

    @GET
    public String view() {
        return service.fetch();
    }
}