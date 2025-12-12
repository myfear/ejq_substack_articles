package com.pixel.filter.impl;

import java.awt.image.BufferedImage;

import com.pixel.filter.ImageFilter;

public class DownsampleFilter implements ImageFilter {
    private final int blockSize;

    public DownsampleFilter(int blockSize) {
        this.blockSize = blockSize;
    }

    @Override
    public BufferedImage apply(BufferedImage img) {
        int w = Math.max(1, img.getWidth() / blockSize);
        int h = Math.max(1, img.getHeight() / blockSize);

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x * blockSize, y * blockSize);
                result.setRGB(x, y, rgb);
            }
        }
        return result;
    }

    @Override
    public String getName() {
        return "downsample";
    }
}
