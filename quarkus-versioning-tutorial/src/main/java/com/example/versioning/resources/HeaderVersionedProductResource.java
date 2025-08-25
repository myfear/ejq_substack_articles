package com.example.versioning.resources;

import java.util.List;

import com.example.versioning.dto.ProductV1;
import com.example.versioning.dto.ProductV2;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/products-header")
@Produces(MediaType.APPLICATION_JSON)
public class HeaderVersionedProductResource {

    @GET
    public Response getProducts(@HeaderParam("X-API-Version") @DefaultValue("1") String version) {
        return switch (version) {
            case "1" -> Response.ok(List.of(
                    new ProductV1("p1", "Laptop", "A powerful laptop for developers."))).build();
            case "2" -> Response.ok(List.of(
                    new ProductV2("p1", "Laptop", "A powerful laptop for developers.", true))).build();
            default -> Response.status(Response.Status.BAD_REQUEST)
                    .entity("Unsupported API version: " + version)
                    .build();
        };
    }
}