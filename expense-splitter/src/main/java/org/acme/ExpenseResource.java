package org.acme;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;

import org.jboss.logging.Logger;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import dev.langchain4j.data.image.Image;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/expenses")
public class ExpenseResource {

    public static Logger LOG = Logger.getLogger(ExpenseResource.class);

    @Inject
    ReceiptProcessor processor;

    @POST
    @Path("/split")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response splitExpense(@RestForm("image") FileUpload receipt, @RestForm Integer nAttendees)
            throws IOException {

        if (receipt == null || nAttendees <= 0) {
            throw new jakarta.ws.rs.BadRequestException("Receipt file and number of attendees are required.");
        }

        ReceiptData receiptData = null;

        try {
            // 1. Read image bytes from the uploaded file
            byte[] imageBytes = Files.readAllBytes(receipt.uploadedFile());

            // 2. Get the MIME type of the uploaded image
            String mimeType = receipt.contentType();
            if (mimeType == null || (!mimeType.equals("image/png") && !mimeType.equals("image/jpeg"))) {
                // Add more supported types if needed by your model
                LOG.error("Mime Type not Supported");
                return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE)
                        .entity("Unsupported image type: " + mimeType)
                        .build();
            }

            String base64String = java.util.Base64.getEncoder().encodeToString(imageBytes);

            // Very LARGE log output, so commented out
            // LOG.info(base64String + " b64");

            // 3. Create a Langchain4j Image object
            Image image = Image.builder()
                    .base64Data(base64String) // Use the base64 encoded string
                    .mimeType(mimeType)
                    .build();

            // 4. Call the AI service

            receiptData = processor.extractTotal(image);

        } catch (Exception e) {
            // Catch other potential exceptions from the AI service
            LOG.error(e.getMessage(), e);
            // return "Error getting description from AI: " + e.getMessage();
        }

        // 5. Calculate the split
        BigDecimal total = BigDecimal.valueOf(receiptData.total());
        BigDecimal attendees = BigDecimal.valueOf(nAttendees);
        BigDecimal splitAmount = total.divide(attendees, 2, RoundingMode.HALF_UP);

        return Response.ok(new SplitResult(receiptData.total(), nAttendees, splitAmount.doubleValue())).build();
    }

    public record SplitResult(double total, int nAttendees, double splitAmount) {
    }
}