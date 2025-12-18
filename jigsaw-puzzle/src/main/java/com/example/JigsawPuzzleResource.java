package com.example;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.jboss.resteasy.reactive.PartType;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/jigsaw")
public class JigsawPuzzleResource {

    @POST
    @Path("/generate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    public Response generateJigsawPuzzle(@BeanParam JigsawRequest request) {
        try {
            int rows = request.rows != null ? request.rows : 4;
            int cols = request.cols != null ? request.cols : 4;

            if (rows < 2 || rows > 20 || cols < 2 || cols > 20) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Grid size must be between 2 and 20")
                        .build();
            }

            BufferedImage inputImage = ImageIO.read(
                    new ByteArrayInputStream(request.image));

            if (inputImage == null) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity("Invalid image format")
                        .build();
            }

            JigsawPuzzleGenerator generator = new JigsawPuzzleGenerator(inputImage, rows, cols);

            BufferedImage puzzleImage = generator.generatePuzzle();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(puzzleImage, "PNG", baos);

            return Response.ok(baos.toByteArray())
                    .header(
                            "Content-Disposition",
                            "attachment; filename=\"jigsaw-puzzle.png\"")
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing image: " + e.getMessage())
                    .build();
        }
    }

    public static class JigsawRequest {

        @FormParam("image")
        @PartType(MediaType.APPLICATION_OCTET_STREAM)
        public byte[] image;

        @FormParam("rows")
        @PartType(MediaType.TEXT_PLAIN)
        public Integer rows;

        @FormParam("cols")
        @PartType(MediaType.TEXT_PLAIN)
        public Integer cols;
    }
}