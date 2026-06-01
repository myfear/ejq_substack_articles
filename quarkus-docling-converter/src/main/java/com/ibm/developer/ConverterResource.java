package com.ibm.developer;

import java.nio.file.Files;

import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import ai.docling.core.DoclingDocument;
import ai.docling.serve.api.convert.request.options.OutputFormat;
import ai.docling.serve.api.convert.response.ConvertDocumentResponse;
import ai.docling.serve.api.convert.response.InBodyConvertDocumentResponse;
import io.quarkiverse.docling.runtime.client.DoclingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/convert")
@Consumes(MediaType.MULTIPART_FORM_DATA)
@Produces(MediaType.APPLICATION_JSON)
public class ConverterResource {

    @Inject
    DoclingService doclingService;

    @POST
    public Response convert(@RestForm("file") FileUpload file) {
        if (file == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Error: No file uploaded.").build();
        }

        try {
            byte[] fileBytes = Files.readAllBytes(file.uploadedFile());

            ConvertDocumentResponse result = doclingService.convertFromBytes(
                    fileBytes,
                    file.fileName(),
                    OutputFormat.JSON);

            if (!(result instanceof InBodyConvertDocumentResponse inBody)) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity("Unexpected response type: " + result.getResponseType()).build();
            }

            DoclingDocument document = inBody.getDocument().getJsonContent();
            return Response.ok(document).build();

        } catch (java.io.IOException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Failed to read uploaded file: " + e.getMessage())
                    .build();
        }
    }
}
