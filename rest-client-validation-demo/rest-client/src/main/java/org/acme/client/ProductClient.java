package org.acme.client;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/products")
@RegisterRestClient(configKey = "product-api")
public interface ProductClient {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    Product getProduct();
}