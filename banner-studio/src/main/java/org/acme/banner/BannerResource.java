package org.acme.banner;

import java.io.IOException;
import java.util.Map;

import io.quarkus.qute.Template;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@RequestScoped
public class BannerResource {

    @Inject
    Template studio; // templates/studio.html
    @Inject
    FigletRenderer renderer;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public String ui() {
        return studio.data("fonts", FigletRenderer.FONTS)
                .data("result", null)
                .data("input", Map.of("text", "Hello Quarkus", "font", "Small.flf", "maxWidth", 80, "fit", true))
                .data("imageAscii", null)
                .render();
    }

    @POST
    @Path("render")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public String render(@FormParam("text") String text,
            @FormParam("font") String font,
            @FormParam("maxWidth") @DefaultValue("80") int maxWidth,
            @FormParam("fit") @DefaultValue("true") boolean fit) throws IOException {

        String ascii = renderer.renderMultilineWithFont(text, font);
        int width = ascii.lines().mapToInt(String::length).max().orElse(0);
        FigletRenderer.RenderResult rr = new FigletRenderer.RenderResult(ascii, font, width, true);

        return studio.data("fonts", FigletRenderer.FONTS)
                .data("input", Map.of("text", text, "font", font, "maxWidth", maxWidth, "fit", fit))
                .data("result", rr)
                .data("imageAscii", null)
                .render();
    }

    @POST
    @Path("export")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces("text/plain")
    public Response export(@FormParam("ascii") String ascii) {
        return Response.ok(ascii)
                .header("Content-Disposition", "attachment; filename=banner.txt")
                .build();
    }
}