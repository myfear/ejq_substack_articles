package com.example;

import java.io.File;

import org.apache.camel.ProducerTemplate;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/upload")
public class FileUploadResource {

    @Inject
    private ProducerTemplate producerTemplate;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadFile(@org.jboss.resteasy.reactive.RestForm FileUpload file) {
        if (file == null || !file.fileName().toLowerCase().endsWith(".pdf")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"A PDF file must be uploaded\"}")
                    .build();
        }

        try {
            // Get the uploaded file directly
            File uploadedFile = file.uploadedFile().toFile();

            // Send the file path to the Camel route and get the result
            String result = producerTemplate.requestBody("direct:processPdf", uploadedFile, String.class);

            return Response.ok(result).build();
        } catch (Exception e) {
            // This will catch the VirusFoundException from our scanner
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("{\"error\":\"Processing failed: " + e.getMessage() + "\"}")
                    .build();
        }
    }
}