package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/color")
@Produces(MediaType.APPLICATION_JSON)
public class ColorResource {

    @Inject
    ColorService colorService;

    @GET
    public ColorService.RGB color(@QueryParam("input") String input) {
        return colorService.generateColor(input == null ? "default" : input);
    }
}