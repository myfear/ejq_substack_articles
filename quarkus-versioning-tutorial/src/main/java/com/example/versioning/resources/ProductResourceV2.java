package com.example.versioning.resources;

import java.util.List;

import com.example.versioning.dto.ProductV2;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v2/products")
@Produces(MediaType.APPLICATION_JSON)
public class ProductResourceV2 {

    @GET
    public Response getProducts() {
        List<ProductV2> products = List.of(
                new ProductV2("p1", "Laptop", "A powerful laptop for developers.", true));
        return Response.ok(products).build();
    }
}