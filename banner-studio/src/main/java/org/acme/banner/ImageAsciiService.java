package org.acme.banner;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Random;

import javax.imageio.ImageIO;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ImageAsciiService {

    // From light (left) to dark (right); we invert later so darker -> denser glyph
    private static final char[] RAMP = " .'`^\",:;Il!i><~+_-?][}{1)(|\\/tfjrxnuvczXYUJCLQ0OZmwqpdbkhao*#MW&8%B@$"
            .toCharArray();

    public String convert(InputStream imageStream, int targetCols, int maxRows, int k) throws Exception {
        BufferedImage src = ImageIO.read(imageStream);
        if (src == null)
            throw new IllegalArgumentException("Unsupported image format");

        BufferedImage scaled = scaleToCols(src, targetCols, maxRows);
        BufferedImage quant = kMeansQuantize(scaled, k, 8);
        return toAscii(quant);
    }

    private static BufferedImage scaleToCols(BufferedImage src, int cols, int maxRows) {
        int w = src.getWidth(), h = src.getHeight();

        // Characters are roughly twice as tall as they are wide in terminals
        double charAspect = 2.0;
        double scale = (double) cols / w;
        int newW = cols;
        int newH = (int) Math.round(h * scale / charAspect);

        if (newH > maxRows) {
            double s2 = (double) maxRows / newH;
            newW = Math.max(1, (int) Math.round(newW * s2));
            newH = maxRows;
        }

        BufferedImage out = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, newW, newH, null);
        g.dispose();
        return out;
    }

    private static BufferedImage kMeansQuantize(BufferedImage img, int k, int iterations) {
        int w = img.getWidth(), h = img.getHeight();
        int[] px = img.getRGB(0, 0, w, h, null, 0, w);

        int[] centroids = new int[k];
        Random rnd = new Random(42);
        for (int i = 0; i < k; i++)
            centroids[i] = px[rnd.nextInt(px.length)];

        int[] assign = new int[px.length];

        for (int it = 0; it < iterations; it++) {
            for (int i = 0; i < px.length; i++) {
                assign[i] = nearest(px[i], centroids);
            }
            long[] sumR = new long[k], sumG = new long[k], sumB = new long[k];
            int[] count = new int[k];
            for (int i = 0; i < px.length; i++) {
                int c = assign[i], rgb = px[i];
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                sumR[c] += r;
                sumG[c] += g;
                sumB[c] += b;
                count[c]++;
            }
            for (int c = 0; c < k; c++) {
                if (count[c] == 0)
                    continue;
                int r = (int) (sumR[c] / count[c]);
                int g = (int) (sumG[c] / count[c]);
                int b = (int) (sumB[c] / count[c]);
                centroids[c] = (0xFF << 24) | (r << 16) | (g << 8) | b;
            }
        }

        for (int i = 0; i < px.length; i++)
            px[i] = centroids[assign[i]];

        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        out.setRGB(0, 0, w, h, px, 0, w);
        return out;
    }

    private static int nearest(int rgb, int[] centroids) {
        int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        int best = 0;
        long bestD = Long.MAX_VALUE;
        for (int i = 0; i < centroids.length; i++) {
            int c = centroids[i];
            int cr = (c >> 16) & 0xFF, cg = (c >> 8) & 0xFF, cb = c & 0xFF;
            long dr = r - cr, dg = g - cg, db = b - cb;
            long d = dr * dr + dg * dg + db * db;
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
    }

    private static String toAscii(BufferedImage img) {
        StringBuilder sb = new StringBuilder(img.getHeight() * (img.getWidth() + 1));
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                double lum = 0.2126 * r + 0.7152 * g + 0.0722 * b; // 0..255
                int idx = (int) Math.round((RAMP.length - 1) * (lum / 255.0));
                idx = (RAMP.length - 1) - idx; // invert: darker -> denser glyph
                sb.append(RAMP[idx]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}