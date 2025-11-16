package com.example.embeddings;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/products")
public class ProductResource {

    @Inject
    ProductImageService service;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/upload")
    public Response upload(
            @RestForm("name") String name,
            @RestForm("image") FileUpload file) throws IOException {
        byte[] bytes = Files.readAllBytes(file.uploadedFile());
        ProductEntity p = service.addProduct(name, bytes);
        return Response.ok(Map.of("id", p.id, "name", p.name)).build();
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Path("/search")
    public List<ProductMatch> search(
            @RestForm("image") FileUpload file,
            @QueryParam("limit") @DefaultValue("5") int limit) throws IOException {
        byte[] bytes = Files.readAllBytes(file.uploadedFile());
        return service.findSimilar(bytes, limit);
    }
}