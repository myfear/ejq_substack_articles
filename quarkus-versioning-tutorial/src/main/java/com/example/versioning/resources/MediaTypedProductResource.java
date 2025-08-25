package com.example.versioning.resources;

import java.util.List;

import com.example.versioning.dto.ProductV1;
import com.example.versioning.dto.ProductV2;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

@Path("/products-media")
public class MediaTypedProductResource {

    @GET
    @Produces("application/vnd.myapi.v1+json")
    public Response getProductsV1() {
        return Response.ok(List.of(
                new ProductV1("p1", "Laptop", "A powerful laptop for developers."))).build();
    }

    @GET
    @Produces("application/vnd.myapi.v2+json")
    public Response getProductsV2() {
        return Response.ok(List.of(
                new ProductV2("p1", "Laptop", "A powerful laptop for developers.", true))).build();
    }
}
