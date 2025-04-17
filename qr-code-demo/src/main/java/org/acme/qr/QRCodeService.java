package org.acme.qr;

import java.util.Objects;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.nayuki.qrcodegen.QrCode;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QRCodeService {
   
    public QrCode generateQrCode(String data) {
    
        QrCode qr = QrCode.encodeText(data, QrCode.Ecc.LOW);  // Make the QR Code symbol
        return qr;
    }

/**
	 * Returns a string of SVG code for an image depicting the specified QR Code, with the specified
	 * number of border modules. The string always uses Unix newlines (\n), regardless of the platform.
	 * @param qr the QR Code to render (not {@code null})
	 * @param border the number of border modules to add, which must be non-negative
	 * @param lightColor the color to use for light modules, in any format supported by CSS, not {@code null}
	 * @param darkColor the color to use for dark modules, in any format supported by CSS, not {@code null}
     * @param download if true, the svg will be downloaded, otherwise it will be displayed in the browser   
	 * @return a string representing the QR Code as an SVG XML document
	 * @throws NullPointerException if any object is {@code null}
	 * @throws IllegalArgumentException if the border is negative
	 */
	public static String toSvgString(QrCode qr, int border, String lightColor, String darkColor, boolean download) {
		Objects.requireNonNull(qr);
		Objects.requireNonNull(lightColor);
		Objects.requireNonNull(darkColor);
		if (border < 0)
			throw new IllegalArgumentException("Border must be non-negative");
		long brd = border;
		StringBuilder sb = new StringBuilder();
        if (download) {
			sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
			sb.append("<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">\n");
            } 
			sb.append(String.format("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" viewBox=\"0 0 %1$d %1$d\" stroke=\"none\">\n",
				qr.size + brd * 2));
			sb.append("\t<rect width=\"100%\" height=\"100%\" fill=\"" + lightColor + "\"/>\n");
			sb.append("\t<path d=\"");
		for (int y = 0; y < qr.size; y++) {
			for (int x = 0; x < qr.size; x++) {
				if (qr.getModule(x, y)) {
					if (x != 0 || y != 0)
						sb.append(" ");
					sb.append(String.format("M%d,%dh1v1h-1z", x + brd, y + brd));
				}
			}
		}
		return sb
			.append("\" fill=\"" + darkColor + "\"/>\n")
			.append("</svg>\n")
			.toString();
	}


 /**
     * Returns a base64 encoded string of the QR Code image
     * @param qr the QR Code to render (not {@code null})
     * @param scale the side length (measured in pixels, must be positive) of each module
     * @param border the number of border modules to add, which must be non-negative
     * @param lightColor the color to use for light modules, in 0xRRGGBB format
     * @param darkColor the color to use for dark modules, in 0xRRGGBB format
     * @return a base64 encoded string of the QR Code image
     * @throws NullPointerException if the QR Code is {@code null}
     * @throws IllegalArgumentException if the scale or border is out of range
     */
    public static String toBase64Image(QrCode qr, int scale, int border, int lightColor, int darkColor) {
        Objects.requireNonNull(qr);
        if (scale <= 0 || border < 0)
            throw new IllegalArgumentException("Value out of range");
        if (border > Integer.MAX_VALUE / 2 || qr.size + border * 2L > Integer.MAX_VALUE / scale)
            throw new IllegalArgumentException("Scale or border too large");

        BufferedImage image = new BufferedImage((qr.size + border * 2) * scale, (qr.size + border * 2) * scale,
                BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                boolean color = qr.getModule(x / scale - border, y / scale - border);
                image.setRGB(x, y, color ? darkColor : lightColor);
            }
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            javax.imageio.ImageIO.write(image, "PNG", baos);
            return java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to encode image to base64", e);
        }
    }

}

