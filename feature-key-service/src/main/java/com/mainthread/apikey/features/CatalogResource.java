package com.mainthread.apikey.features;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/catalog")
@Produces(MediaType.APPLICATION_JSON)
public class CatalogResource {

    @GET
    @Path("/products")
    @RolesAllowed("catalog:read")
    public String products() {
        return "{\"status\":\"ok\",\"data\":\"visible to catalog:read\"}";
    }

    @GET
    @Path("/admin-report")
    @RolesAllowed("catalog:admin")
    public String adminReport() {
        return "{\"status\":\"ok\",\"data\":\"visible to catalog:admin\"}";
    }
}