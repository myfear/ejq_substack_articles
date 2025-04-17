package org.acme.qr;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import static java.util.Objects.requireNonNull;

@Path("/some-page")
public class QRCodePage {

    @Inject
    QRCodeService qrCodeService;


    private final Template qrcode;

    public QRCodePage(Template qrcode) {
        this.qrcode = requireNonNull(qrcode, "page is required");
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("url") String url) {
        return qrcode.data("base64Image", QRCodeService.toBase64Image(qrCodeService.generateQrCode(url), 4, 4, 0x000000, 0xFFFFFF));
    }

}
