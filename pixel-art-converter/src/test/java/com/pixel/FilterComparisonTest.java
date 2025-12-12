package com.pixel;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.pixel.filter.impl.FloydSteinbergFilter;

class FilterComparisonTest {

    @Test
    void testDithering() {
        BufferedImage src = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = src.createGraphics();
        g.setColor(new Color(140, 140, 140));
        g.fillRect(0, 0, 10, 10);
        g.dispose();

        var dither = new FloydSteinbergFilter();
        BufferedImage out = dither.apply(src);

        Set<Integer> colors = new HashSet<>();
        for (int y = 0; y < out.getHeight(); y++) {
            for (int x = 0; x < out.getWidth(); x++) {
                colors.add(out.getRGB(x, y));
            }
        }

        Assertions.assertTrue(colors.size() > 1,
                "Dithering should create variance");
    }
}