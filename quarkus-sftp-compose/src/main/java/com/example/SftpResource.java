package com.example;

import java.io.InputStream;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/files")
public class SftpResource {

    @Inject
    SftpService sftpService;

    @POST
    @Path("/upload/{fileName}")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@PathParam("fileName") String fileName, @HeaderParam("Content-Length") long fileSize,
            InputStream fileInputStream) {
        try {
            FileMetadata meta = sftpService.uploadFile(fileName, fileSize, fileInputStream);
            return Response.status(Response.Status.CREATED).entity(meta).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to upload file: " + e.getMessage()).build();
        }
    }

    @GET
    @Path("/meta/{fileName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getMetadata(@PathParam("fileName") String fileName) {
        return FileMetadata.find("fileName", fileName)
                .firstResultOptional()
                .map(meta -> Response.ok(meta).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    @GET
    @Path("/download/{fileName}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public Response downloadFile(@PathParam("fileName") String fileName) {
        // This method remains the same as before
        try {
            InputStream inputStream = sftpService.downloadFile(fileName);
            return Response.ok(inputStream).header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                    .build();
        } catch (Exception e) {
            if (e instanceof com.jcraft.jsch.SftpException
                    && ((com.jcraft.jsch.SftpException) e).id == com.jcraft.jsch.ChannelSftp.SSH_FX_NO_SUCH_FILE) {
                return Response.status(Response.Status.NOT_FOUND).entity("File not found: " + fileName).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Failed to download file: " + e.getMessage()).build();
        }
    }
}