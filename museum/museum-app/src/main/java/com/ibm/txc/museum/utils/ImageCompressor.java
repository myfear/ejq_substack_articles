package com.ibm.txc.museum.utils;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Base64;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageCompressor {

    public static String condense(File inputFile, int maxSize, float quality) throws Exception {
        // Load
        BufferedImage original = ImageIO.read(inputFile);

        // Compute scale
        int width = original.getWidth();
        int height = original.getHeight();
        double scale = Math.min(1.0, (double) maxSize / Math.max(width, height));
        int newW = (int) (width * scale);
        int newH = (int) (height * scale);

        // Resize
        BufferedImage resized = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(original, 0, 0, newW, newH, null);
        g.dispose();

        // Compress to JPEG
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageWriter jpgWriter = ImageIO.getImageWritersByFormatName("jpg").next();
        ImageWriteParam jpgWriteParam = jpgWriter.getDefaultWriteParam();
        jpgWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        jpgWriteParam.setCompressionQuality(quality);

        jpgWriter.setOutput(new MemoryCacheImageOutputStream(baos));
        jpgWriter.write(null, new IIOImage(resized, null, null), jpgWriteParam);
        jpgWriter.dispose();

        byte[] compressedBytes = baos.toByteArray();

        // Encode to base64
        return Base64.getEncoder().encodeToString(compressedBytes);
    }
}
