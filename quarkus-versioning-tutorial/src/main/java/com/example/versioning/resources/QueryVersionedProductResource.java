package com.example.versioning.resources;

import java.util.List;

import com.example.versioning.dto.ProductV1;
import com.example.versioning.dto.ProductV2;
import com.example.versioning.exceptions.UnsupportedVersionException;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/products-query")
@Produces(MediaType.APPLICATION_JSON)
public class QueryVersionedProductResource {

    @GET
    public Response getProducts(@QueryParam("v") @DefaultValue("1") String version) {
        return switch (version) {
            case "1" -> Response.ok(List.of(
                    new ProductV1("p1", "Laptop", "A powerful laptop for developers."))).build();
            case "2" -> Response.ok(List.of(
                    new ProductV2("p1", "Laptop", "A powerful laptop for developers.", true))).build();
            default -> throw new UnsupportedVersionException("Unsupported version: " + version);
        };
    }
}