package org.acme;

import jakarta.enterprise.context.ApplicationScoped;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.FontMetrics;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;

@ApplicationScoped
public class MemeService {

    private Font memeFont;

    // Load the font from resources
    // Ensure the font file is placed in src/main/resources/Impact.ttf
    public MemeService() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("Impact.ttf")) {
            if (is == null)
                throw new IllegalStateException("Font not found");
            memeFont = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(120f);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load font", e);
        }
    }

    // Generates a meme image with the specified top and bottom text
    // This method reads a base image (boromir.png) from resources,
    // draws the specified text on it, and returns the image as a byte array.
    public byte[] generateMeme(String topText, String bottomText) {
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("META-INF/resources/boromir.png");
            if (is == null)
                throw new IllegalStateException("Image not found");
            BufferedImage image = ImageIO.read(is);

            Graphics2D g = image.createGraphics();
            g.setFont(memeFont);
            g.setColor(Color.WHITE);

            drawText(g, topText, image.getWidth(), image.getHeight(), true);
            drawText(g, bottomText, image.getWidth(), image.getHeight(), false);
            g.dispose();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate meme", e);
        }
    }

    // Draws the specified text on the image at the top or bottom
    private void drawText(Graphics2D g, String text, int width, int height, boolean isTop) {
        if (text == null || text.isEmpty())
            return;

        FontMetrics metrics = g.getFontMetrics();
        int x = (width - metrics.stringWidth(text)) / 2;
        int y = isTop ? metrics.getHeight() : height - metrics.getHeight() / 4;

        // Outline
        g.setColor(Color.BLACK);
        g.drawString(text.toUpperCase(), x - 2, y - 2);
        g.drawString(text.toUpperCase(), x + 2, y - 2);
        g.drawString(text.toUpperCase(), x - 2, y + 2);
        g.drawString(text.toUpperCase(), x + 2, y + 2);

        // Main text
        g.setColor(Color.WHITE);
        g.drawString(text.toUpperCase(), x, y);
    }
}