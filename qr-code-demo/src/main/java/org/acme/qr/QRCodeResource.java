package org.acme.qr;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

@Path("/qr")
public class QRCodeResource {

    @Inject
    QRCodeService qrCodeService;


    @GET
    @Path("/download/svg")
    @Produces(MediaType.APPLICATION_SVG_XML)
    public Response getQrCodeSVGDownload(@QueryParam("text") String text) {
        if (text == null || text.isBlank()) {
            return Response
                .status(Status.BAD_REQUEST)
                .entity("QR Code data cannot be null or empty")
                .type(MediaType.TEXT_PLAIN)
                .header("Content-Disposition", "attachment; filename=qrcode.svg")
                .build();
        }
        String svg = QRCodeService.toSvgString(qrCodeService.generateQrCode(text), 4, "#FFFFFF", "#000000", true);
        return Response.ok(svg)
            .header("Content-Disposition", "attachment; filename=qr-code.svg")
            .build();
    }



    
}
