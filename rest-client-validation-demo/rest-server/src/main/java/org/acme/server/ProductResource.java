package org.acme.server;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/products")
public class ProductResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getProduct() {
        // Simulate a bad response from an upstream system
        return Map.of(
                "name", "",
                "price", 0);
    }
}