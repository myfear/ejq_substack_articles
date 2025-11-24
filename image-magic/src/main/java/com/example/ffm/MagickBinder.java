package com.example.ffm;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;

@ApplicationScoped
public class MagickBinder {

    // --- Private Fields (Method Handles) ---
    private MethodHandle newMagickWand;
    private MethodHandle newPixelWand;
    private MethodHandle magickReadImageBlob;
    private MethodHandle magickGetImageBlob;
    private MethodHandle magickSetFormat;
    private MethodHandle magickGetImageWidth;
    private MethodHandle magickGetImageHeight;
    private MethodHandle magickSetImageAlphaChannel;
    private MethodHandle magickRelinquishMemory;
    private MethodHandle magickReadImage;
    private MethodHandle magickResizeImage;
    private MethodHandle magickScaleImage;
    private MethodHandle magickPolaroidImage;
    private MethodHandle newDrawingWand;
    private MethodHandle destroyDrawingWand;
    private MethodHandle pixelSetColor;
    private MethodHandle magickSetImagePage;
    private MethodHandle magickAddImage;
    private MethodHandle magickMergeImageLayers;
    private MethodHandle magickTransformImageColorspace;
    private MethodHandle magickGetNumberImages;
    private MethodHandle magickSetImageBackgroundColor;
    private MethodHandle magickSetBackgroundColor;
    private MethodHandle magickSetImageBorderColor;
    private MethodHandle magickRotateImage;
    private MethodHandle magickResetIterator;
    private MethodHandle magickAnnotateImage;
    private MethodHandle drawSetFontSize;
    private MethodHandle drawSetFillColor;
    private MethodHandle drawSetFont;

    @PostConstruct
    void init() {
        Linker linker = Linker.nativeLinker();

        // 1. Explicit Library Loading (Critical for macOS/Homebrew)
        try {
            System.load("/opt/homebrew/lib/libMagickWand-7.Q16HDRI.dylib");
            Log.info("ImageMagick library loaded from /opt/homebrew/lib!");
        } catch (UnsatisfiedLinkError e) {
            Log.error("CRITICAL: Failed to load ImageMagick.");
            Log.error("Please check that /opt/homebrew/lib/libMagickWand-7.Q16HDRI.dylib exists.");
            throw e;
        }

        SymbolLookup lib = SymbolLookup.loaderLookup();

        try {
            // 2. Bind Core Functions

            // Wand Creation
            newMagickWand = linker.downcallHandle(
                    lib.find("NewMagickWand").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS));

            newPixelWand = linker.downcallHandle(
                    lib.find("NewPixelWand").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS));

            pixelSetColor = linker.downcallHandle(
                    lib.find("PixelSetColor").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            newDrawingWand = linker.downcallHandle(
                    lib.find("NewDrawingWand").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS));

            destroyDrawingWand = linker.downcallHandle(
                    lib.find("DestroyDrawingWand").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            magickSetFormat = linker.downcallHandle(
                    lib.find("MagickSetImageFormat").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            magickSetImageAlphaChannel = linker.downcallHandle(
                    lib.find("MagickSetImageAlphaChannel").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

            // Read (Files/Blobs)
            magickReadImageBlob = linker.downcallHandle(
                    lib.find("MagickReadImageBlob").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                            ValueLayout.JAVA_LONG));

            // Read (Files)
            magickReadImage = linker.downcallHandle(
                    lib.find("MagickReadImage").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // Resize Image
            magickResizeImage = linker.downcallHandle(
                    lib.find("MagickResizeImage").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, // Return
                            ValueLayout.ADDRESS, // Wand
                            ValueLayout.JAVA_LONG, // Columns
                            ValueLayout.JAVA_LONG, // Rows
                            ValueLayout.JAVA_INT, // Filter (e.g., Lanczos = 22)
                            ValueLayout.JAVA_DOUBLE // Blur
                    ));

            // Scale Image (simpler scaling, used for percentage resizes)
            magickScaleImage = linker.downcallHandle(
                    lib.find("MagickScaleImage").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, // Return
                            ValueLayout.ADDRESS, // Wand
                            ValueLayout.JAVA_LONG, // Columns
                            ValueLayout.JAVA_LONG // Rows
                    ));

            // Polaroid Effect
            // Signature: MagickPolaroidImage(wand, drawing_wand, caption, angle, method)
            magickPolaroidImage = linker.downcallHandle(
                    lib.find("MagickPolaroidImage").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, // Return
                            ValueLayout.ADDRESS, // Wand
                            ValueLayout.ADDRESS, // Drawing Wand
                            ValueLayout.ADDRESS, // Caption (const char *, can be NULL)
                            ValueLayout.JAVA_DOUBLE, // Angle
                            ValueLayout.JAVA_INT // PixelInterpolateMethod (e.g., UndefinedInterpolatePixel = 0)
                    ));

            magickGetImageBlob = linker.downcallHandle(
                    lib.find("MagickGetImageBlob").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // Dimensions
            magickGetImageWidth = linker.downcallHandle(
                    lib.find("MagickGetImageWidth").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

            magickGetImageHeight = linker.downcallHandle(
                    lib.find("MagickGetImageHeight").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

            // Page/Virtual Canvas positioning (for two-stage positioning)
            magickSetImagePage = linker.downcallHandle(
                    lib.find("MagickSetImagePage").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, // Return
                            ValueLayout.ADDRESS, // Wand
                            ValueLayout.JAVA_LONG, // Width
                            ValueLayout.JAVA_LONG, // Height
                            ValueLayout.JAVA_LONG, // X offset
                            ValueLayout.JAVA_LONG // Y offset
                    ));

            // Add image to sequence
            magickAddImage = linker.downcallHandle(
                    lib.find("MagickAddImage").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // Merge image layers
            magickMergeImageLayers = linker.downcallHandle(
                    lib.find("MagickMergeImageLayers").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

            magickTransformImageColorspace = linker.downcallHandle(
                    lib.find("MagickTransformImageColorspace").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));

            // Get number of images in sequence
            magickGetNumberImages = linker.downcallHandle(
                    lib.find("MagickGetNumberImages").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));

            // Background/Border color setting
            magickSetImageBackgroundColor = linker.downcallHandle(
                    lib.find("MagickSetImageBackgroundColor").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            magickSetBackgroundColor = linker.downcallHandle(
                    lib.find("MagickSetBackgroundColor").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            magickSetImageBorderColor = linker.downcallHandle(
                    lib.find("MagickSetImageBorderColor").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // Rotate image
            magickRotateImage = linker.downcallHandle(
                    lib.find("MagickRotateImage").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE));

            // Reset iterator
            magickResetIterator = linker.downcallHandle(
                    lib.find("MagickResetIterator").orElseThrow(),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS));

            // Annotate image with text
            magickAnnotateImage = linker.downcallHandle(
                    lib.find("MagickAnnotateImage").orElseThrow(),
                    FunctionDescriptor.of(
                            ValueLayout.JAVA_INT, // Return
                            ValueLayout.ADDRESS, // MagickWand
                            ValueLayout.ADDRESS, // DrawingWand
                            ValueLayout.JAVA_DOUBLE, // x
                            ValueLayout.JAVA_DOUBLE, // y
                            ValueLayout.JAVA_DOUBLE, // angle (for rotation)
                            ValueLayout.ADDRESS // text (const char *)
                    ));

            // DrawingWand text properties
            drawSetFontSize = linker.downcallHandle(
                    lib.find("DrawSetFontSize").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_DOUBLE));

            drawSetFillColor = linker.downcallHandle(
                    lib.find("DrawSetFillColor").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // Set font family
            drawSetFont = linker.downcallHandle(
                    lib.find("DrawSetFont").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            magickRelinquishMemory = linker.downcallHandle(
                    lib.find("MagickRelinquishMemory").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            // Genesis (Library Startup)
            MethodHandle genesis = linker.downcallHandle(
                    lib.find("MagickWandGenesis").orElseThrow(),
                    FunctionDescriptor.ofVoid());
            genesis.invoke();

        } catch (Throwable e) {
            throw new RuntimeException("Failed to bind ImageMagick functions", e);
        }
    }

    // --- Getters (Must use these to access fields through the Proxy) ---
    public MethodHandle getNewMagickWand() {
        return newMagickWand;
    }

    public MethodHandle getNewPixelWand() {
        return newPixelWand;
    }

    public MethodHandle getMagickReadImageBlob() {
        return magickReadImageBlob;
    }

    public MethodHandle getMagickGetImageBlob() {
        return magickGetImageBlob;
    }

    public MethodHandle getMagickSetFormat() {
        return magickSetFormat;
    }

    public MethodHandle getMagickSetImageAlphaChannel() {
        return magickSetImageAlphaChannel;
    }

    public MethodHandle getMagickGetImageWidth() {
        return magickGetImageWidth;
    }

    public MethodHandle getMagickGetImageHeight() {
        return magickGetImageHeight;
    }

    public MethodHandle getMagickRelinquishMemory() {
        return magickRelinquishMemory;
    }

    public MethodHandle getMagickReadImage() {
        return magickReadImage;
    }

    public MethodHandle getMagickResizeImage() {
        return magickResizeImage;
    }

    public MethodHandle getMagickScaleImage() {
        return magickScaleImage;
    }

    public MethodHandle getMagickPolaroidImage() {
        return magickPolaroidImage;
    }

    public MethodHandle getNewDrawingWand() {
        return newDrawingWand;
    }

    public MethodHandle getDestroyDrawingWand() {
        return destroyDrawingWand;
    }

    public MethodHandle getPixelSetColor() {
        return pixelSetColor;
    }

    public MethodHandle getMagickSetImagePage() {
        return magickSetImagePage;
    }

    public MethodHandle getMagickAddImage() {
        return magickAddImage;
    }

    public MethodHandle getMagickMergeImageLayers() {
        return magickMergeImageLayers;
    }

    public MethodHandle getMagickTransformImageColorspace() {
        return magickTransformImageColorspace;
    }

    public MethodHandle getMagickGetNumberImages() {
        return magickGetNumberImages;
    }

    public MethodHandle getMagickSetImageBackgroundColor() {
        return magickSetImageBackgroundColor;
    }

    public MethodHandle getMagickSetBackgroundColor() {
        return magickSetBackgroundColor;
    }

    public MethodHandle getMagickSetImageBorderColor() {
        return magickSetImageBorderColor;
    }

    public MethodHandle getMagickRotateImage() {
        return magickRotateImage;
    }

    public MethodHandle getMagickResetIterator() {
        return magickResetIterator;
    }

    public MethodHandle getMagickAnnotateImage() {
        return magickAnnotateImage;
    }

    public MethodHandle getDrawSetFontSize() {
        return drawSetFontSize;
    }

    public MethodHandle getDrawSetFillColor() {
        return drawSetFillColor;
    }

    public MethodHandle getDrawSetFont() {
        return drawSetFont;
    }
}