package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.CsvUploadMetadata;
import org.acme.service.CsvUploadService;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("/csv")
public class CsvUploadResource {

    @Inject
    CsvUploadService csvUploadService;

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "Upload a CSV and create a dynamic table", description = "Uploads a CSV file, stores it in a dynamically created table, and returns metadata.")
    @APIResponse(responseCode = "200", description = "Upload successful")
    public CsvUploadMetadata uploadCsv(@FormParam("file") FileUpload fileUpload) throws Exception {
        return csvUploadService.uploadCsv(fileUpload);
    }
}
