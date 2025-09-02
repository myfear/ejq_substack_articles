package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/summarize")
@Produces(MediaType.TEXT_PLAIN)
public class SummaryResource {

    @Inject
    AiSummarizer summarizer;

    @GET
    public Response summarize(@QueryParam("url") String url) {
        if (url == null || url.isBlank()) {
            throw new BadRequestException("Missing ?url=");
        }
        try {
            String result = summarizer.summarize(url);
            return Response.ok(result).build();
        } catch (Exception e) {
            // Most failures are handled by FT; this is the last resort.
            return Response.serverError()
                    .entity("Unexpected error: " + e.getMessage())
                    .build();
        }
    }
}