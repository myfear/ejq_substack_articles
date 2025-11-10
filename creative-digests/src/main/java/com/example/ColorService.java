package com.example;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ColorService {

    public record RGB(int r, int g, int b) {
        public String toHex() {
            return String.format("#%02x%02x%02x", r, g, b);
        }
    }

    public RGB generateColor(String input) {
        byte[] hash = DigestUtils.sha256(input);
        int r = hash[0] & 0xFF;
        int g = hash[1] & 0xFF;
        int b = hash[2] & 0xFF;

        int max = Math.max(Math.max(r, g), b);
        if (max < 128) {
            double factor = 200.0 / max;
            r = (int) Math.min(255, r * factor);
            g = (int) Math.min(255, g * factor);
            b = (int) Math.min(255, b * factor);
        }

        return new RGB(r, g, b);
    }
}