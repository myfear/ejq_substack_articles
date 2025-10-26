package org.acme.client;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;

@Path("/validated-products")
public class ProductResource {

    @Inject
    ProductService service;

    @GET
    public Response getValidatedProduct() {
        return Response.ok(service.getValidatedProduct()).build();
    }
}