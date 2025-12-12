package com.pixel.filter.impl;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import com.pixel.filter.ImageFilter;

public class UpsampleFilter implements ImageFilter {
    private final int scale;

    public UpsampleFilter(int scale) {
        this.scale = scale;
    }

    @Override
    public BufferedImage apply(BufferedImage img) {
        int w = img.getWidth() * scale;
        int h = img.getHeight() * scale;

        BufferedImage result = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.drawImage(img, 0, 0, w, h, null);
        g.dispose();

        return result;
    }

    @Override
    public String getName() {
        return "upsample";
    }
}