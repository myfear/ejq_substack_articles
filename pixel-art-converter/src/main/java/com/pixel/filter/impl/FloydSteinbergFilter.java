package com.pixel.filter.impl;

import java.awt.image.BufferedImage;

import com.pixel.filter.ImageFilter;

public class FloydSteinbergFilter implements ImageFilter {

    private static final int RED = 0;
    private static final int GREEN = 1;
    private static final int BLUE = 2;

    @Override
    public BufferedImage apply(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();

        int[][][] buffer = new int[w][h][3];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                buffer[x][y][RED] = (rgb >> 16) & 0xFF;
                buffer[x][y][GREEN] = (rgb >> 8) & 0xFF;
                buffer[x][y][BLUE] = rgb & 0xFF;
            }
        }

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int oldR = buffer[x][y][RED];
                int oldG = buffer[x][y][GREEN];
                int oldB = buffer[x][y][BLUE];

                int factor = 32;
                int newR = Math.round(factor * Math.round(oldR / (float) factor));
                int newG = Math.round(factor * Math.round(oldG / (float) factor));
                int newB = Math.round(factor * Math.round(oldB / (float) factor));

                newR = Math.min(255, Math.max(0, newR));
                newG = Math.min(255, Math.max(0, newG));
                newB = Math.min(255, Math.max(0, newB));

                buffer[x][y][RED] = newR;
                buffer[x][y][GREEN] = newG;
                buffer[x][y][BLUE] = newB;

                int errR = oldR - newR;
                int errG = oldG - newG;
                int errB = oldB - newB;

                distributeError(buffer, x + 1, y, errR, errG, errB, 7.0 / 16);
                distributeError(buffer, x - 1, y + 1, errR, errG, errB, 3.0 / 16);
                distributeError(buffer, x, y + 1, errR, errG, errB, 5.0 / 16);
                distributeError(buffer, x + 1, y + 1, errR, errG, errB, 1.0 / 16);
            }
        }

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int r = buffer[x][y][RED];
                int g = buffer[x][y][GREEN];
                int b = buffer[x][y][BLUE];
                result.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        return result;
    }

    private void distributeError(int[][][] buffer, int x, int y,
            int er, int eg, int eb, double weight) {
        if (x < 0 || x >= buffer.length || y < 0 || y >= buffer[0].length)
            return;
        buffer[x][y][RED] += (int) (er * weight);
        buffer[x][y][GREEN] += (int) (eg * weight);
        buffer[x][y][BLUE] += (int) (eb * weight);
    }

    @Override
    public String getName() {
        return "dither";
    }
}