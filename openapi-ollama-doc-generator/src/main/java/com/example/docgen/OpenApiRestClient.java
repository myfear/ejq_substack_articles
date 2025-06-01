package com.example.docgen;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient(baseUri = "http://localhost:8080") // Points to our own application
@Path("/q/openapi")
public interface OpenApiRestClient {

    @GET
    @Produces("application/yaml") // Quarkus's default OpenAPI format is YAML
    String getOpenApiYaml();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    String getOpenApiJson(@QueryParam("format") String format); // Allows requesting JSON explicitly
}