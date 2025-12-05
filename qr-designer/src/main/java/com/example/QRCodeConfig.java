package com.example;

public record QRCodeConfig(
        String text,
        Integer size,
        String patternType, // solid, gradient, radial, checker, wave
        String shapeType, // square, dots, rounded, diamond, hex, heart
        String finderType, // square, rounded, circle
        String colorPrimary,
        String colorSecondary,
        Boolean transparentBackground,
        String logoPath,
        Integer logoSize) {
}