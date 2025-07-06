package com.example;

import java.net.URI;
import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
public class FileResource {

    @Inject
    MinioClient minio;
    @Inject
    FileStorageService files;
    
    @ConfigProperty(name = "minio.bucket")
    String bucket;

    @POST
    @Path("upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@RestForm("file") FileUpload file, @RestForm String downloads) throws Exception {
        String id = UUID.randomUUID().toString();
        String objectName = id + "-" + file.fileName();

        minio.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectName)
                        .stream(Files.newInputStream(file.filePath()), file.size(), -1)
                        .contentType(
                                Optional.ofNullable(file.contentType())
                                        .orElse("application/octet-stream"))
                        .build());

        Integer allowedDownloads;

        try {
            allowedDownloads = Integer.parseInt(downloads);

        } catch (NumberFormatException e) {
            System.out.println("Invalid number format: " + downloads);
            allowedDownloads = 1; // Default to 1 download
        }

        files.put(id, new FileMetadata(
                file.fileName(),
                objectName,
                allowedDownloads,
                new AtomicInteger()));

        return Response.ok("http://localhost:8080/download/" + id).build();
    }

    @GET
    @Path("download/{id}")
    public Response download(@PathParam("id") String id) {
        return files.get(id)
                .map(meta -> meta.stillDownloadable()
                        ? redirectViaPresignedUrl(meta)
                        : expired(id))
                .orElse(Response.status(Response.Status.NOT_FOUND).entity("File not found").build());
    }

    private Response redirectViaPresignedUrl(FileMetadata meta) {
        try {
            meta.markServed();
            String url = minio.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(meta.objectName())
                            .expiry(5, TimeUnit.MINUTES)
                            .build());
            return Response.seeOther(URI.create(url)).build();
        } catch (Exception e) {
            return Response.serverError().build();
        }
    }

    private Response expired(String id) {
        files.delete(id);
        return Response.status(Response.Status.NOT_FOUND).entity("This link has expired").build();
    }
}
