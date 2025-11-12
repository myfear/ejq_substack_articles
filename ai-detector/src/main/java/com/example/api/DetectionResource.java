package com.example.api;

import com.example.service.AnalysisService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;

@Path("/api/detect")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DetectionResource {

    @Inject
    AnalysisService service;

    @POST
    public Map<String, Object> detect(Map<String, String> body) {
        String text = body.getOrDefault("text", "");
        return service.analyze(text);
    }
}