package org.acme.badge;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Base64;

import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/badge")
@ApplicationScoped
public class BadgeResource {

    @Inject
    BadgeGenerator generator;

    private String quarkusIconBase64;

    public BadgeResource() {
        try (InputStream is = getClass().getResourceAsStream("/quarkus-icon.svg")) {
            quarkusIconBase64 = Base64.getEncoder().encodeToString(is.readAllBytes());
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Quarkus icon", e);
        }
    }

    @GET
    @Path("/dynamic.svg")
    @Produces("image/svg+xml")
    public String svg(
            @QueryParam("label") @DefaultValue("Built with") String label,
            @QueryParam("value") @DefaultValue("Quarkus") String value,
            @QueryParam("theme") @DefaultValue("default") String theme) {
        return generator.createBadge(label, value, theme, quarkusIconBase64);
    }

    @GET
    @Path("/dynamic.png")
    @Produces("image/png")
    public Response png(
            @QueryParam("label") @DefaultValue("Powered by") String label,
            @QueryParam("value") @DefaultValue("Quarkus 3.x") String value,
            @QueryParam("theme") @DefaultValue("default") String theme) throws Exception {

        String svgContent = svg(label, value, theme);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        PNGTranscoder pngTranscoder = new PNGTranscoder();
        pngTranscoder.transcode(
                new TranscoderInput(new StringReader(svgContent)),
                new TranscoderOutput(out));
        out.flush();

        return Response.ok(out.toByteArray())
                .type("image/png")
                .build();
    }
}