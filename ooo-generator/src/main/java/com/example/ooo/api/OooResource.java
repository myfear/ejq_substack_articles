package com.example.ooo.api;

import com.example.ooo.service.OooService;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/ooo")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OooResource {

    @Inject
    OooService service;

    @POST
    public OooResponse generate(@Valid OooRequest request) {
        return service.generate(request);
    }
}