package com.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/screenshot")
public class ScreenshotResource {

    @Inject
    ScreenshotService screenshotService;

    @GET
    @Produces("image/png")
    public Response captureScreenshot(@QueryParam("url") String url) {
        if (url == null || url.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("URL parameter is required")
                    .build();
        }

        try {
            byte[] screenshot = screenshotService.captureHomepage(url);
            return Response.ok(screenshot)
                    .header("Content-Disposition", "attachment; filename=\"screenshot.png\"")
                    .type("image/png")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error capturing screenshot: " + e.getMessage())
                    .build();
        }
    }
}
