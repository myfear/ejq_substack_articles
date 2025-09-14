package com.acme.totp.api;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrCode.Ecc;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * Streams a PNG QR code for a given data string.
 * Quarkiverse QRCodeGen pulls in Nayuki's generator.
 */
@Path("/qr")
public class QrResource {

    @GET
    @Produces("image/png")
    public Response png(@QueryParam("data") String data,
            @QueryParam("size") @DefaultValue("256") int size) throws Exception {
        if (data == null || data.isBlank()) {
            return Response.status(400).entity("Missing ?data").build();
        }
        QrCode qr = QrCode.encodeText(data, Ecc.MEDIUM);
        BufferedImage img = toImage(qr, size, size);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "PNG", out);
        return Response.ok(out.toByteArray()).build();
    }

    private static BufferedImage toImage(QrCode qr, int width, int height) {
        int scale = Math.max(1, Math.min(width, height) / (qr.size + 2)); // 1 module border
        int imgSize = (qr.size + 2) * scale;
        BufferedImage img = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < imgSize; y++) {
            for (int x = 0; x < imgSize; x++) {
                img.setRGB(x, y, 0xFFFFFF);
            }
        }
        for (int y = 0; y < qr.size; y++) {
            for (int x = 0; x < qr.size; x++) {
                int color = qr.getModule(x, y) ? 0x000000 : 0xFFFFFF;
                for (int dy = 0; dy < scale; dy++) {
                    for (int dx = 0; dx < scale; dx++) {
                        img.setRGB((x + 1) * scale + dx, (y + 1) * scale + dy, color);
                    }
                }
            }
        }
        return img;
    }
}
