package com.example.resource;

import java.util.Map;

import com.example.service.JsonbService;
import com.example.service.JsonpService;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/json")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JsonDemoResource {
    @Inject
    JsonbService jsonbService;
    @Inject
    JsonpService jsonpService;

    @GET
    @Path("/jsonb/product")
    public Response jsonbProduct() {
        return Response.ok(jsonbService.serializeProduct(jsonbService.sampleProduct())).build();
    }

    @GET
    @Path("/jsonp/product")
    public Response jsonpProduct() {
        return Response.ok(jsonpService.buildProductJson(1L, "Tablet", 599.99, 75)).build();
    }

    @POST
    @Path("/jsonp/discount")
    public Response applyDiscount(String json, @QueryParam("percent") double percent) {
        return Response.ok(jsonpService.applyDiscount(json, percent)).build();
    }

    @GET
    @Path("/compare")

    public Response compare() {
        return Response.ok(Map.of(
                "jsonb", "Object mapping for REST APIs",
                "jsonp", "Fine-grained control for dynamic or large JSON")).build();
    }
}