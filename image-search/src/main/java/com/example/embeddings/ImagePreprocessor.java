package com.example.embeddings;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public class ImagePreprocessor {

    private final int width;
    private final int height;

    // CLIP normalization
    private static final float[] MEAN = { 0.48145466f, 0.4578275f, 0.40821073f };
    private static final float[] STD = { 0.26862954f, 0.26130258f, 0.27577711f };

    public ImagePreprocessor(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public float[] process(BufferedImage image) {
        BufferedImage resized = resize(image);

        float[] out = new float[3 * width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                int rgb = resized.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                int idx = y * width + x;

                out[idx] = (r / 255f - MEAN[0]) / STD[0];
                out[idx + width * height] = (g / 255f - MEAN[1]) / STD[1];
                out[idx + 2 * width * height] = (b / 255f - MEAN[2]) / STD[2];
            }
        }
        return out;
    }

    private BufferedImage resize(BufferedImage original) {
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();
        return out;
    }
}