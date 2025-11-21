package com.example;

public class FaceDetectionResult {
    private final byte[] annotatedImage; // Original image with bounding box overlay
    private final byte[] faceImage; // Extracted face for emotion analysis
    private final int x;
    private final int y;
    private final int width;
    private final int height;
    private final double confidence; // Approximate confidence based on detection parameters

    public FaceDetectionResult(byte[] annotatedImage, byte[] faceImage, int x, int y, int width, int height, double confidence) {
        this.annotatedImage = annotatedImage;
        this.faceImage = faceImage;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.confidence = confidence;
    }

    public byte[] getAnnotatedImage() {
        return annotatedImage;
    }

    public byte[] getFaceImage() {
        return faceImage;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public double getConfidence() {
        return confidence;
    }
}

