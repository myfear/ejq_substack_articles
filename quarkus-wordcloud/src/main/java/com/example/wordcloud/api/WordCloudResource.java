package com.example.wordcloud.api;

import java.awt.image.BufferedImage;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import com.example.wordcloud.app.WordCloudService;

import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

@Path("/api/wordcloud.png")
public class WordCloudResource {

    @Inject
    WordCloudService service;

    @GET
    @Produces("image/png")
    public Response renderPng(@QueryParam("text") String text,
            @BeanParam QueryParams qp) {
        CloudParams p = qp.toParams();
        BufferedImage img = service.render(text, p);
        StreamingOutput stream = (OutputStream out) -> ImageIO.write(img, "PNG", out);
        return Response.ok(stream).build(); // chunked streaming
    }

    public static class QueryParams {
        @QueryParam("w")
        @DefaultValue("900")
        int w;
        @QueryParam("h")
        @DefaultValue("600")
        int h;
        @QueryParam("max")
        @DefaultValue("60")
        int max;
        @QueryParam("minFont")
        @DefaultValue("12")
        int minFont;
        @QueryParam("maxFont")
        @DefaultValue("72")
        int maxFont;
        @QueryParam("rotate")
        @DefaultValue("true")
        boolean rotate;
        @QueryParam("rotateProb")
        @DefaultValue("0.25")
        double rotateProb;
        @QueryParam("font")
        @DefaultValue("IBM Plex Sans")
        String font;
        @QueryParam("rewordle")
        @DefaultValue("true")
        boolean rewordle;
        @QueryParam("seed")
        @DefaultValue("42")
        long seed;

        CloudParams toParams() {
            CloudParams p = new CloudParams();
            p.width = w;
            p.height = h;
            p.maxWords = max;
            p.minFont = minFont;
            p.maxFont = maxFont;
            p.rotateSome = rotate;
            p.rotateProb = rotateProb;
            p.fontFamily = font;
            p.localRewordle = rewordle;
            p.seed = seed;
            return p;
        }
    }
}