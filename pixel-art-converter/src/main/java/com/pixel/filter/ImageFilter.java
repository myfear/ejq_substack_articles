package com.pixel.filter;

import java.awt.image.BufferedImage;

public interface ImageFilter {
    BufferedImage apply(BufferedImage image);

    String getName();
}