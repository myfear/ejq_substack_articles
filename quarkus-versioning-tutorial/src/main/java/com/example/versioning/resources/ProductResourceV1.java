package com.example.versioning.resources;

import java.util.List;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import com.example.versioning.dto.ProductV1;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/v1/products")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Products (V1)", description = "Legacy product operations")
public class ProductResourceV1 {

    @GET
    @Operation(summary = "Get all products", description = "Returns products in v1 format.")
    public Response getProducts() {
        List<ProductV1> products = List.of(
                new ProductV1("p1", "Laptop", "A powerful laptop for developers."));
        return Response.ok(products).build();
    }
}