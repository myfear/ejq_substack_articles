package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/whitepaper")
public class WhitePaperResource {

    @Inject
    WhitePaperAiService aiService;

    @Inject
    PdfGeneratorService pdfService;

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces("application/pdf")
    public Response generateWhitePaper(WhitePaperRequest request) {
        try {
            String content = aiService.generateWhitePaperContent(
                    request.productName(),
                    request.targetAudience(),
                    request.features());

            byte[] pdf = pdfService.createWhitePaperPdf(content);

            return Response.ok(pdf)
                    .header("Content-Disposition", "attachment; filename=Innovatech_White_Paper.pdf")
                    .build();
        } catch (Exception e) {
            return Response.serverError().entity(e.getMessage()).build();
        }
    }
}