package com.example.service;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.foreign.MemorySegment;

import javax.imageio.ImageIO;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ImageProcessorService {

    @Inject
    FFmpegFilterService filterService;

    private BufferedImage logoImage;

    @PostConstruct
    void init() {
        try {
            // Load Logo using standard Java
            File logoFile = new File("src/main/resources/logo.png");
            if (logoFile.exists()) {
                this.logoImage = ImageIO.read(logoFile);
                Log.infof("Logo loaded: %dx%d", logoImage.getWidth(), logoImage.getHeight());
            } else {
                Log.error("Logo not found!");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] overlayLogo(MemorySegment rawBgrData, int width, int height) {
        try {
            // 1. Create a Java Image that wraps the raw bytes
            // TYPE_3BYTE_BGR matches the FFmpeg output perfectly
            BufferedImage frame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);

            // Get access to the image's internal byte array
            byte[] imagePixelData = ((DataBufferByte) frame.getRaster().getDataBuffer()).getData();

            // Copy FFmpeg native memory -> Java Heap Array
            // (This copy is very fast, typically <1ms for HD frames)
            MemorySegment.copy(rawBgrData, 0, MemorySegment.ofArray(imagePixelData), 0, imagePixelData.length);

            // 2. Draw the Logo
            Graphics2D g2d = frame.createGraphics();

            // High quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (logoImage != null) {
                // Scale logo to 50% of original size
                int scaledWidth = logoImage.getWidth() / 2;
                int scaledHeight = logoImage.getHeight() / 2;
                
                // Position in upper right corner
                int x = width - scaledWidth - 20;
                int y = 20;

                // Java 2D handles PNG transparency automatically
                g2d.drawImage(logoImage, x, y, scaledWidth, scaledHeight, null);
            }
            g2d.dispose();

            // 3. Export to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(frame, "jpg", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Java2D Processing Failed", e);
        }
    }

    /**
     * Overlay logo on a BufferedImage (for webcam frames)
     * Applies hqdn3d denoising filter before overlaying the logo
     */
    public byte[] overlayLogo(BufferedImage frame) {
        try {
            int width = frame.getWidth();
            int height = frame.getHeight();

            // Apply hqdn3d denoising filter
            BufferedImage denoisedFrame = filterService.applyHqdn3d(frame);
            if (denoisedFrame == null) {
                denoisedFrame = frame; // Fallback to original if denoising fails
            }

            // Create a copy to avoid modifying the original
            BufferedImage processedFrame = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = processedFrame.createGraphics();

            // High quality rendering
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            // Draw the denoised frame
            g2d.drawImage(denoisedFrame, 0, 0, null);

            // Overlay the logo
            if (logoImage != null) {
                // Scale logo to 50% of original size
                int scaledWidth = logoImage.getWidth() / 2;
                int scaledHeight = logoImage.getHeight() / 2;
                
                // Position in upper right corner
                int x = width - scaledWidth - 20;
                int y = 20;
                
                g2d.drawImage(logoImage, x, y, scaledWidth, scaledHeight, null);
            }

            g2d.dispose();

            // Export to JPEG
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(processedFrame, "jpg", baos);
            return baos.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Image Processing Failed", e);
        }
    }
}