package com.pixel.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import com.pixel.service.FilterChainService;
import com.pixel.service.FilterConfigDTO;
import com.pixel.service.FilterFactory;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/filter")
public class ImageFilterResource {

    @Inject
    FilterChainService chainService;
    @Inject
    FilterFactory factory;

    @POST
    @Path("/pixelate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces("image/png")
    @RunOnVirtualThread
    public Response pixelate(
            @RestForm("image") FileUpload image,
            @RestForm @DefaultValue("8") int blockSize) {

        try {
            BufferedImage img = ImageIO.read(image.uploadedFile().toFile());
            BufferedImage processed = chainService.applyChain(img,
                    factory.createChain(List.of(
                            new FilterConfigDTO("downsample", Map.of("blockSize", blockSize)),
                            new FilterConfigDTO("dither", Map.of()),
                            new FilterConfigDTO("upsample", Map.of("scale", blockSize)))));
            return writeImage(processed);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }

    private Response writeImage(BufferedImage img) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(img, "PNG", baos);
            return Response.ok(baos.toByteArray()).build();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}