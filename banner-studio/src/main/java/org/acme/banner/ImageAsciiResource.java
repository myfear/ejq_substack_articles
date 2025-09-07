package org.acme.banner;

import java.io.InputStream;
import java.util.Map;

import org.jboss.resteasy.reactive.RestForm;

import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/image")
public class ImageAsciiResource {

    @Inject
    Template studio; // reuse same page
    @Inject
    ImageAsciiService service;

    @POST
    @Path("/convert")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.TEXT_HTML)
    public String convert(@RestForm("file") InputStream file,
            @RestForm("cols") @DefaultValue("80") int cols,
            @RestForm("maxRows") @DefaultValue("60") int maxRows,
            @RestForm("k") @DefaultValue("8") int k) {
        String ascii;
        try {
            ascii = service.convert(file, cols, maxRows, k);
        } catch (Exception e) {
            ascii = "Conversion failed: " + e.getMessage();
        }
        return studio.data("fonts", FigletRenderer.FONTS)
                .data("result", null)
                .data("input", Map.of("text", "Hello Quarkus", "font", "Small.flf", "maxWidth", 80, "fit", true))
                .data("imageAscii", ascii)
                .render();
    }
}