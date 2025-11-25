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

/**
 * REST resource for converting images to ASCII art.
 * <p>
 * This resource provides an endpoint to upload an image file and convert it
 * to ASCII art representation using k-means color quantization and character
 * mapping based on luminance values.
 * </p>
 */
@Path("/image")
public class ImageAsciiResource {

    @Inject
    Template studio; // reuse same page
    @Inject
    ImageAsciiService service;

    /**
     * Converts an uploaded image file to ASCII art.
     * <p>
     * The conversion process involves:
     * <ol>
     *   <li>Scaling the image to fit within the specified column and row constraints</li>
     *   <li>Applying k-means color quantization to reduce the color palette</li>
     *   <li>Mapping pixel luminance values to ASCII characters</li>
     * </ol>
     * The result is rendered in the studio HTML template.
     * </p>
     *
     * @param file    the image file input stream to convert (multipart form data)
     * @param cols    the target number of columns for the ASCII output (default: 80)
     * @param maxRows the maximum number of rows for the ASCII output (default: 60)
     * @param k       the number of color clusters for k-means quantization (default: 8)
     * @return HTML string rendered from the studio template containing the ASCII art result
     */
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