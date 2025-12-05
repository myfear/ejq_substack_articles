package com.example;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QRCodeService {

    public byte[] generate(QRCodeConfig cfg) {
        try {
            BufferedImage image = render(cfg);
            return toPng(image);
        } catch (Exception e) {
            throw new RuntimeException("QR generation failed", e);
        }
    }

    private BufferedImage render(QRCodeConfig cfg) throws Exception {
        // Provide defaults for nullable wrapper types
        int size = cfg.size() != null ? cfg.size() : 300;
        boolean transparentBackground = cfg.transparentBackground() != null ? cfg.transparentBackground() : false;

        // ZXing setup
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode(
                cfg.text(),
                BarcodeFormat.QR_CODE,
                size,
                size,
                hints);

        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        applyQualityHints(g);

        // Background
        if (!transparentBackground) {
            g.setColor(parseColor(cfg.colorSecondary(), Color.WHITE));
            g.fillRect(0, 0, size, size);
        }

        int modules = matrix.getWidth();
        int moduleSize = size / modules;

        // Texture setup
        Paint paint = createPaint(cfg, size);
        g.setPaint(paint);

        // Draw QR modules
        for (int x = 0; x < modules; x++) {
            for (int y = 0; y < modules; y++) {
                if (matrix.get(x, y)) {
                    drawModule(g, x, y, moduleSize, cfg);
                }
            }
        }

        // Finder patterns
        drawFinderPatterns(g, moduleSize, cfg);

        // Logo
        if (cfg.logoPath() != null && !cfg.logoPath().isBlank()) {
            drawLogo(g, cfg);
        }

        g.dispose();
        return img;
    }

    private void applyQualityHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
    }

    private Paint createPaint(QRCodeConfig cfg, int size) {
        Color primary = parseColor(cfg.colorPrimary(), Color.BLACK);
        Color secondary = parseColor(cfg.colorSecondary(), Color.WHITE);

        return switch (cfg.patternType()) {
            case "gradient" -> new GradientPaint(0, 0, primary, size, size, secondary);
            case "radial" -> new RadialGradientPaint(
                    size / 2f, size / 2f, size / 2f,
                    new float[] { 0f, 1f },
                    new Color[] { primary, secondary });
            case "checker" -> {
                int tileSize = size / 8;
                BufferedImage pattern = new BufferedImage(tileSize * 2, tileSize * 2, BufferedImage.TYPE_INT_ARGB);
                Graphics2D pg = pattern.createGraphics();
                pg.setColor(primary);
                pg.fillRect(0, 0, tileSize * 2, tileSize * 2);
                pg.setColor(secondary);
                pg.fillRect(0, 0, tileSize, tileSize);
                pg.fillRect(tileSize, tileSize, tileSize, tileSize);
                pg.dispose();
                yield new TexturePaint(pattern, new Rectangle2D.Float(0, 0, tileSize * 2, tileSize * 2));
            }
            case "wave" -> {
                int waveSize = size / 4;
                BufferedImage pattern = new BufferedImage(waveSize, waveSize, BufferedImage.TYPE_INT_ARGB);
                Graphics2D pg = pattern.createGraphics();
                pg.setColor(primary);
                pg.fillRect(0, 0, waveSize, waveSize);
                pg.setColor(secondary);
                Path2D wave = new Path2D.Float();
                for (int i = 0; i < waveSize; i += 2) {
                    wave.moveTo(i, waveSize / 2 + (int) (Math.sin(i * 0.1) * 5));
                    wave.lineTo(i + 1, waveSize / 2 + (int) (Math.sin((i + 1) * 0.1) * 5));
                }
                pg.setStroke(new java.awt.BasicStroke(2));
                pg.draw(wave);
                pg.dispose();
                yield new TexturePaint(pattern, new Rectangle2D.Float(0, 0, waveSize, waveSize));
            }
            default -> primary; // solid
        };
    }

    private void drawModule(Graphics2D g, int x, int y, int moduleSize, QRCodeConfig cfg) {
        int px = x * moduleSize;
        int py = y * moduleSize;

        switch (cfg.shapeType()) {
            case "dots" -> {
                int centerX = px + moduleSize / 2;
                int centerY = py + moduleSize / 2;
                int radius = moduleSize / 2 - 1;
                g.fill(new Ellipse2D.Float(centerX - radius, centerY - radius, radius * 2, radius * 2));
            }
            case "rounded" -> {
                int cornerRadius = moduleSize / 4;
                g.fill(new RoundRectangle2D.Float(px, py, moduleSize, moduleSize, cornerRadius, cornerRadius));
            }
            case "diamond" -> {
                Path2D diamond = new Path2D.Float();
                diamond.moveTo(px + moduleSize / 2, py);
                diamond.lineTo(px + moduleSize, py + moduleSize / 2);
                diamond.lineTo(px + moduleSize / 2, py + moduleSize);
                diamond.lineTo(px, py + moduleSize / 2);
                diamond.closePath();
                g.fill(diamond);
            }
            case "hex" -> {
                Path2D hex = new Path2D.Float();
                int centerX = px + moduleSize / 2;
                int centerY = py + moduleSize / 2;
                int radius = moduleSize / 2;
                for (int i = 0; i < 6; i++) {
                    double angle = Math.PI / 3 * i;
                    double hx = centerX + radius * Math.cos(angle);
                    double hy = centerY + radius * Math.sin(angle);
                    if (i == 0) {
                        hex.moveTo(hx, hy);
                    } else {
                        hex.lineTo(hx, hy);
                    }
                }
                hex.closePath();
                g.fill(hex);
            }
            case "heart" -> {
                Path2D heart = new Path2D.Float();
                int centerX = px + moduleSize / 2;
                int centerY = py + moduleSize / 2;
                int size = moduleSize / 2;
                heart.moveTo(centerX, centerY + size / 2);
                heart.curveTo(centerX, centerY, centerX - size / 2, centerY - size / 2, centerX - size / 2, centerY);
                heart.curveTo(centerX - size / 2, centerY + size / 2, centerX, centerY + size / 2, centerX,
                        centerY + size / 2);
                heart.curveTo(centerX, centerY + size / 2, centerX + size / 2, centerY + size / 2, centerX + size / 2,
                        centerY);
                heart.curveTo(centerX + size / 2, centerY - size / 2, centerX, centerY, centerX, centerY + size / 2);
                heart.closePath();
                g.fill(heart);
            }
            default -> g.fillRect(px, py, moduleSize, moduleSize); // square
        }
    }

    private void drawFinderPatterns(Graphics2D g, int moduleSize, QRCodeConfig cfg) {
        int size = cfg.size() != null ? cfg.size() : 300;
        int modules = size / moduleSize;

        // Three finder patterns at corners
        int[][] positions = { { 0, 0 }, { modules - 7, 0 }, { 0, modules - 7 } };

        for (int[] pos : positions) {
            int startX = pos[0] * moduleSize;
            int startY = pos[1] * moduleSize;
            int patternSize = 7 * moduleSize;

            Color finderColor = parseColor(cfg.colorPrimary(), Color.BLACK);
            g.setColor(finderColor);

            switch (cfg.finderType()) {
                case "rounded" -> {
                    int cornerRadius = moduleSize;
                    g.fill(new RoundRectangle2D.Float(startX, startY, patternSize, patternSize, cornerRadius,
                            cornerRadius));
                    // Inner square
                    g.setColor(parseColor(cfg.colorSecondary(), Color.WHITE));
                    g.fill(new RoundRectangle2D.Float(
                            startX + moduleSize, startY + moduleSize,
                            5 * moduleSize, 5 * moduleSize,
                            cornerRadius / 2, cornerRadius / 2));
                    // Center square
                    g.setColor(finderColor);
                    g.fill(new RoundRectangle2D.Float(
                            startX + 2 * moduleSize, startY + 2 * moduleSize,
                            3 * moduleSize, 3 * moduleSize,
                            cornerRadius / 3, cornerRadius / 3));
                }
                case "circle" -> {
                    g.fill(new Ellipse2D.Float(startX, startY, patternSize, patternSize));
                    g.setColor(parseColor(cfg.colorSecondary(), Color.WHITE));
                    g.fill(new Ellipse2D.Float(
                            startX + moduleSize, startY + moduleSize,
                            5 * moduleSize, 5 * moduleSize));
                    g.setColor(finderColor);
                    g.fill(new Ellipse2D.Float(
                            startX + 2 * moduleSize, startY + 2 * moduleSize,
                            3 * moduleSize, 3 * moduleSize));
                }
                default -> { // square
                    g.fillRect(startX, startY, patternSize, patternSize);
                    g.setColor(parseColor(cfg.colorSecondary(), Color.WHITE));
                    g.fillRect(startX + moduleSize, startY + moduleSize, 5 * moduleSize, 5 * moduleSize);
                    g.setColor(finderColor);
                    g.fillRect(startX + 2 * moduleSize, startY + 2 * moduleSize, 3 * moduleSize, 3 * moduleSize);
                }
            }
        }
    }

    private void drawLogo(Graphics2D g, QRCodeConfig cfg) {
        try {
            java.io.File logoFile = new java.io.File(cfg.logoPath());
            if (!logoFile.exists()) {
                return;
            }

            BufferedImage logo = ImageIO.read(logoFile);
            if (logo == null) {
                return;
            }

            int size = cfg.size() != null ? cfg.size() : 300;
            int logoSize = cfg.logoSize() != null ? cfg.logoSize() : 60;
            int centerX = size / 2 - logoSize / 2;
            int centerY = size / 2 - logoSize / 2;

            // Scale logo to fit
            BufferedImage scaledLogo = new BufferedImage(logoSize, logoSize, BufferedImage.TYPE_INT_ARGB);
            Graphics2D lg = scaledLogo.createGraphics();
            applyQualityHints(lg);
            lg.drawImage(logo, 0, 0, logoSize, logoSize, null);
            lg.dispose();

            g.drawImage(scaledLogo, centerX, centerY, null);
        } catch (IOException e) {
            // Silently fail if logo can't be loaded
        }
    }

    private byte[] toPng(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", baos);
        return baos.toByteArray();
    }

    private Color parseColor(String colorStr, Color defaultColor) {
        if (colorStr == null || colorStr.isBlank()) {
            return defaultColor;
        }

        try {
            // Handle hex colors like #RRGGBB or #AARRGGBB
            if (colorStr.startsWith("#")) {
                String hex = colorStr.substring(1);
                if (hex.length() == 6) {
                    return new Color(
                            Integer.parseInt(hex.substring(0, 2), 16),
                            Integer.parseInt(hex.substring(2, 4), 16),
                            Integer.parseInt(hex.substring(4, 6), 16));
                } else if (hex.length() == 8) {
                    return new Color(
                            Integer.parseInt(hex.substring(0, 2), 16),
                            Integer.parseInt(hex.substring(2, 4), 16),
                            Integer.parseInt(hex.substring(4, 6), 16),
                            Integer.parseInt(hex.substring(6, 8), 16));
                }
            }

            // Try to use Color field names (WHITE, BLACK, etc.)
            try {
                java.lang.reflect.Field field = Color.class.getField(colorStr.toUpperCase());
                return (Color) field.get(null);
            } catch (Exception e) {
                // Not a named color
            }

            return defaultColor;
        } catch (Exception e) {
            return defaultColor;
        }
    }
}
