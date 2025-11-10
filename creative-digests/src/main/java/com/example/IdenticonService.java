package com.example;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class IdenticonService {

    private static final int GRID = 5;
    private static final int SIZE = 40;

    public byte[] generate(String input) {
        byte[] hash = DigestUtils.md5(input);
        int imageSize = GRID * SIZE;
        BufferedImage image = new BufferedImage(imageSize, imageSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        // background color
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageSize, imageSize);

        // foreground color
        ColorService.RGB rgb = new ColorService().generateColor(input);
        g.setColor(new Color(rgb.r(), rgb.g(), rgb.b()));

        int index = 0;
        for (int row = 0; row < GRID; row++) {
            for (int col = 0; col < (GRID + 1) / 2; col++) {
                boolean fill = (hash[index++] & 0xFF) % 2 == 0;
                if (fill) {
                    g.fillRect(col * SIZE, row * SIZE, SIZE, SIZE);
                    int mirror = GRID - 1 - col;
                    g.fillRect(mirror * SIZE, row * SIZE, SIZE, SIZE);
                }
            }
        }

        g.dispose();
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}