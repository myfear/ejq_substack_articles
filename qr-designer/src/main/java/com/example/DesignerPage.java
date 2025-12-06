package com.example;

import java.util.Base64;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/designer")
public class DesignerPage {

    @Inject
    Template designer;
    @Inject
    QRCodeService service;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance showForm() {
        return designer.data("qrImage", null);
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_JSON)
    public TemplateInstance generate(QRCodeConfig cfg) {

        byte[] png = service.generate(cfg);
        String base64 = Base64.getEncoder().encodeToString(png);

        return designer
                .data("qrImage", base64)
                .data("text", cfg.text())
                .data("size", cfg.size());
    }

}
