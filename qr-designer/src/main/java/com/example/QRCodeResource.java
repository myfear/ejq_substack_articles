package com.example;

import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/qrcode")
public class QRCodeResource {

    private final QRCodeService qr;

    public QRCodeResource(QRCodeService qr) {
        this.qr = qr;
    }

    @POST
    @Produces("image/png")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response generate(@BeanParam QRCodeConfig cfg) {
        byte[] png = qr.generate(cfg);
        return Response.ok(png)
                .header("Content-Disposition", "attachment; filename=qr.png")
                .build();
    }
}