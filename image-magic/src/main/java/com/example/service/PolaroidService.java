package com.example.service;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.example.ffm.MagickBinder;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class PolaroidService {

    @Inject
    MagickBinder binder;

    boolean debugAnnotations = Log.isDebugEnabled();

    /**
     * Creates a single polaroid image with the specified parameters.
     * This is the extracted working method from OnePolaroidService.
     * 
     * @param wand                     The MagickWand containing the image to
     *                                 process
     * @param arena                    The memory arena for allocations
     * @param borderColor              Border color for the polaroid frame (e.g.,
     *                                 "rgb(248, 248, 248)")
     * @param backgroundColor          Background color for shadow (e.g., "rgba(0,
     *                                 0, 0, 0.4)")
     * @param fontName                 Font name for caption (e.g., "Arial")
     * @param fontSize                 Font size for caption
     * @param caption                  Caption text (null for no caption)
     * @param rotationAngle            Rotation angle in degrees
     * @param maxThumbSize             Maximum thumbnail size (0 to use
     *                                 percentage-based sizing)
     * @param thumbPercentage          Percentage of original size for thumbnail
     *                                 (used if maxThumbSize is 0)
     * @param resizeAfter              Whether to resize to 50% after polaroid
     *                                 effect
     * @param setTransparentBackground Whether to set background to transparent
     *                                 after polaroid
     * @return The processed wand (image is modified in place)
     * @throws Throwable If image processing fails
     */
    private MemorySegment createSinglePolaroid(
            MemorySegment wand,
            Arena arena,
            String borderColor,
            String backgroundColor,
            String fontName,
            double fontSize,
            String caption,
            double rotationAngle,
            long maxThumbSize,
            int thumbPercentage,
            boolean resizeAfter,
            boolean setTransparentBackground) throws Throwable {

        // Get original dimensions
        long origWidth = (long) binder.getMagickGetImageWidth().invoke(wand);
        long origHeight = (long) binder.getMagickGetImageHeight().invoke(wand);

        // Calculate thumbnail size
        long thumbWidth, thumbHeight;
        if (maxThumbSize > 0) {
            // Use max size approach (like OnePolaroidService)
            if (origWidth > origHeight) {
                thumbWidth = maxThumbSize;
                thumbHeight = (maxThumbSize * origHeight) / origWidth;
            } else {
                thumbHeight = maxThumbSize;
                thumbWidth = (maxThumbSize * origWidth) / origHeight;
            }
        } else {
            // Use percentage approach (like original PolaroidService)
            long longerDimension = Math.max(origWidth, origHeight);
            long minTargetSize = (longerDimension * thumbPercentage) / 100;
            if (origWidth > origHeight) {
                thumbWidth = minTargetSize;
                thumbHeight = (minTargetSize * origHeight) / origWidth;
            } else {
                thumbHeight = minTargetSize;
                thumbWidth = (minTargetSize * origWidth) / origHeight;
            }
        }

        // Resize to thumbnail
        int thumbnailResult = (int) binder.getMagickResizeImage().invoke(wand, thumbWidth, thumbHeight, 22, 1.0); // Lanczos
                                                                                                                  // filter
        if (thumbnailResult == 0) {
            throw new RuntimeException("Failed to thumbnail image");
        }

        // Set border color
        MemorySegment borderColorWand = (MemorySegment) binder.getNewPixelWand().invoke();
        binder.getPixelSetColor().invoke(borderColorWand, arena.allocateFrom(borderColor));
        int borderResult = (int) binder.getMagickSetImageBorderColor().invoke(wand, borderColorWand);
        if (borderResult == 0) {
            Log.warn("WARNING: Failed to set border color");
        }

        // Set background color
        MemorySegment backgroundColorWand = (MemorySegment) binder.getNewPixelWand().invoke();
        binder.getPixelSetColor().invoke(backgroundColorWand, arena.allocateFrom(backgroundColor));
        int bgWandResult = (int) binder.getMagickSetBackgroundColor().invoke(wand, backgroundColorWand);
        if (bgWandResult == 0) {
            Log.warn("WARNING: Failed to set wand background color");
        }
        int bgImageResult = (int) binder.getMagickSetImageBackgroundColor().invoke(wand, backgroundColorWand);
        if (bgImageResult == 0) {
            Log.warn("WARNING: Failed to set image background color");
        }

        // Create DrawingWand and set font properties
        MemorySegment drawingWand = (MemorySegment) binder.getNewDrawingWand().invoke();
        if (fontName != null) {
            int fontResult = (int) binder.getDrawSetFont().invoke(drawingWand, arena.allocateFrom(fontName));
            if (fontResult == 0) {
                Log.warn("WARNING: Failed to set font");
            }
        }
        if (fontSize > 0) {
            int fontSizeResult = (int) binder.getDrawSetFontSize().invoke(drawingWand, fontSize);
            if (fontSizeResult == 0) {
                Log.warn("WARNING: Failed to set font size");
            }
        }

        // Prepare caption
        MemorySegment captionStr = caption != null ? arena.allocateFrom(caption) : MemorySegment.NULL;

        // Apply polaroid effect
        int polaroidResult = (int) binder.getMagickPolaroidImage().invoke(
                wand,
                drawingWand,
                captionStr,
                rotationAngle,
                0 // UndefinedInterpolatePixel
        );
        if (polaroidResult == 0) {
            throw new RuntimeException("Failed to apply polaroid effect");
        }

        // Clean up drawing wand
        binder.getDestroyDrawingWand().invoke(drawingWand);

        // Set transparent background if requested
        if (setTransparentBackground) {
            MemorySegment transparentColorWand = (MemorySegment) binder.getNewPixelWand().invoke();
            binder.getPixelSetColor().invoke(transparentColorWand, arena.allocateFrom("none"));
            int transparentBgResult = (int) binder.getMagickSetImageBackgroundColor().invoke(wand,
                    transparentColorWand);
            if (transparentBgResult == 0) {
                Log.warn("WARNING: Failed to set image background to transparent");
            }
            int alphaResult = (int) binder.getMagickSetImageAlphaChannel().invoke(wand, 1); // ActivateAlphaChannel = 1
            if (alphaResult == 0) {
                Log.warn("WARNING: Failed to enable alpha channel");
            }
        }

        // Resize to 50% if requested
        if (resizeAfter) {
            long newWidth = thumbWidth / 2;
            long newHeight = thumbHeight / 2;
            int scaleResult = (int) binder.getMagickScaleImage().invoke(wand, newWidth, newHeight);
            if (scaleResult == 0) {
                throw new RuntimeException("Failed to scale image");
            }
        }

        return wand;
    }

    /**
     * Creates a single polaroid image from uploaded image data with default
     * settings.
     * 
     * @param imageData The image data as a byte array (PNG or JPG)
     * @return Byte array containing the processed polaroid image (PNG format)
     * @throws RuntimeException If image processing fails
     */
    public byte[] createPolaroidFromBytes(byte[] imageData) {
        return createPolaroidFromBytes(
                imageData,
                "Lavender", // border color
                "#000000", // background color (for shadow)
                null, // font name (no caption)
                0.0, // font size (no caption)
                null, // caption (no caption)
                0.0, // rotation angle
                0L, // max thumb size (0 = use percentage)
                10, // 10% of original size
                false, // don't resize after
                true // set transparent background
        );
    }

    /**
     * Creates a single polaroid image from uploaded image data with configurable
     * parameters.
     * 
     * @param imageData                The image data as a byte array (PNG or JPG)
     * @param borderColor              Border color for the polaroid frame (e.g.,
     *                                 "rgb(248, 248, 248)" or "Lavender")
     * @param backgroundColor          Background color for shadow (e.g., "rgba(0,
     *                                 0, 0, 0.4)" or "#000000")
     * @param fontName                 Font name for caption (e.g., "Arial") - null
     *                                 for no caption
     * @param fontSize                 Font size for caption - 0.0 for no caption
     * @param caption                  Caption text - null for no caption
     * @param rotationAngle            Rotation angle in degrees
     * @param maxThumbSize             Maximum thumbnail size (0 to use
     *                                 percentage-based sizing)
     * @param thumbPercentage          Percentage of original size for thumbnail
     *                                 (used if maxThumbSize is 0)
     * @param resizeAfter              Whether to resize to 50% after polaroid
     *                                 effect
     * @param setTransparentBackground Whether to set background to transparent
     *                                 after polaroid
     * @return Byte array containing the processed polaroid image (PNG format)
     * @throws RuntimeException If image processing fails
     */
    public byte[] createPolaroidFromBytes(
            byte[] imageData,
            String borderColor,
            String backgroundColor,
            String fontName,
            double fontSize,
            String caption,
            double rotationAngle,
            long maxThumbSize,
            int thumbPercentage,
            boolean resizeAfter,
            boolean setTransparentBackground) {
        try (Arena arena = Arena.ofConfined()) {
            Log.info("Creating polaroid effect from uploaded image...");

            MethodHandle newWandHandle = binder.getNewMagickWand();

            // 1. Create new wand and read image from blob
            MemorySegment wand = (MemorySegment) newWandHandle.invoke();
            MemorySegment blobSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, imageData);

            int readResult = (int) binder.getMagickReadImageBlob().invoke(wand, blobSegment, (long) imageData.length);
            if (readResult == 0) {
                throw new RuntimeException("Failed to read image from blob");
            }

            long origWidth = (long) binder.getMagickGetImageWidth().invoke(wand);
            long origHeight = (long) binder.getMagickGetImageHeight().invoke(wand);
            Log.info("Image loaded: " + origWidth + "x" + origHeight);

            // Use the extracted method to create polaroid with provided parameters
            createSinglePolaroid(
                    wand,
                    arena,
                    borderColor,
                    backgroundColor,
                    fontName,
                    fontSize,
                    caption,
                    rotationAngle,
                    maxThumbSize,
                    thumbPercentage,
                    resizeAfter,
                    setTransparentBackground);
            Log.info("Polaroid effect applied");

            // Export as PNG
            binder.getMagickSetFormat().invoke(wand, arena.allocateFrom("PNG"));

            MemorySegment lengthPtr = arena.allocate(ValueLayout.JAVA_LONG);
            MemorySegment blobPtr = (MemorySegment) binder.getMagickGetImageBlob().invoke(wand, lengthPtr);

            if (blobPtr == MemorySegment.NULL) {
                throw new RuntimeException("Failed to get image blob");
            }

            long blobSize = lengthPtr.get(ValueLayout.JAVA_LONG, 0);
            Log.info("Exported polaroid: " + blobSize + " bytes");

            // Copy the blob data to a Java byte array before Arena closes
            byte[] result = blobPtr.reinterpret(blobSize).toArray(ValueLayout.JAVA_BYTE);

            // Free the ImageMagick-allocated memory
            binder.getMagickRelinquishMemory().invoke(blobPtr);

            return result;

        } catch (Throwable e) {
            throw new RuntimeException("Polaroid generation failed", e);
        }
    }

    /**
     * Creates individual polaroid images following ImageMagick command-line
     * example:
     * magick test.jpg -thumbnail 120x120 -bordercolor AliceBlue -background
     * SteelBlue4 -polaroid 5 polaroid_1.png
     */
    public byte[] createCollage(List<Path> imagePaths) {
        try (Arena arena = Arena.ofConfined()) {
            Log.info("Creating collage from " + imagePaths.size() + " images...");

            MethodHandle newWandHandle = binder.getNewMagickWand();
            List<MemorySegment> polaroidWands = new ArrayList<>();

            // Create individual polaroids from each image
            for (int i = 0; i < imagePaths.size(); i++) {
                Path imagePath = imagePaths.get(i);
                Log.info("Processing image " + (i + 1) + "/" + imagePaths.size() + ": " + imagePath.getFileName());

                // Create new wand and read image
                MemorySegment wand = (MemorySegment) newWandHandle.invoke();
                MemorySegment pathStr = arena.allocateFrom(imagePath.toAbsolutePath().toString());

                int readResult = (int) binder.getMagickReadImage().invoke(wand, pathStr);
                if (readResult == 0) {
                    Log.warn("Failed to read image " + imagePath.getFileName() + ", skipping");
                    continue;
                }

                long origWidth = (long) binder.getMagickGetImageWidth().invoke(wand);
                long origHeight = (long) binder.getMagickGetImageHeight().invoke(wand);
                Log.info("Image loaded: " + origWidth + "x" + origHeight);

                // Apply polaroid effect
                try {
                    createSinglePolaroid(
                            wand,
                            arena,
                            "Lavender", // border color
                            "#000000", // background color (for shadow)
                            null, // font name (no caption)
                            0.0, // font size (no caption)
                            null, // caption (no caption)
                            0.0, // rotation angle (applied later in collage)
                            0L, // max thumb size (0 = use percentage)
                            10, // 10% of original size
                            false, // don't resize after
                            false // don't set transparent background (for collage)
                    );
                    polaroidWands.add(wand);
                    Log.info("  ✓ Polaroid effect applied");
                } catch (Throwable e) {
                    Log.error("ERROR: " + e.getMessage());
                    // Wand will be cleaned up by arena
                }
            }

            if (polaroidWands.isEmpty()) {
                throw new RuntimeException("No valid images could be processed");
            }

            Log.info("Created " + polaroidWands.size() + " polaroid images");
            Log.info("Composing collage...");

            // Compose collage from polaroid wands
            return composeCollageFromWands(polaroidWands, arena);

        } catch (Throwable e) {
            throw new RuntimeException("Polaroid generation failed", e);
        }
    }

    /**
     * Creates a collage from multiple image byte arrays.
     * 
     * @param imageDataList List of image data as byte arrays (PNG or JPG)
     * @return Byte array containing the processed collage image (PNG format)
     * @throws RuntimeException If image processing fails
     */
    public byte[] createCollageFromBytes(List<byte[]> imageDataList) {
        try (Arena arena = Arena.ofConfined()) {
            Log.info("Creating collage from " + imageDataList.size() + " images...");

            MethodHandle newWandHandle = binder.getNewMagickWand();
            List<MemorySegment> polaroidWands = new ArrayList<>();

            // Create individual polaroids from each image
            for (int i = 0; i < imageDataList.size(); i++) {
                byte[] imageData = imageDataList.get(i);
                Log.info("Processing image " + (i + 1) + "/" + imageDataList.size());

                // Create wand and read image from blob
                MemorySegment wand = (MemorySegment) newWandHandle.invoke();
                MemorySegment blobSegment = arena.allocateFrom(ValueLayout.JAVA_BYTE, imageData);

                int readResult = (int) binder.getMagickReadImageBlob().invoke(
                        wand, blobSegment, (long) imageData.length);
                if (readResult == 0) {
                    Log.warn("Failed to read image " + (i + 1) + ", skipping");
                    continue;
                }

                long origWidth = (long) binder.getMagickGetImageWidth().invoke(wand);
                long origHeight = (long) binder.getMagickGetImageHeight().invoke(wand);
                Log.info("Image loaded: " + origWidth + "x" + origHeight);

                // Apply polaroid effect
                try {
                    createSinglePolaroid(
                            wand,
                            arena,
                            "Lavender", // border color
                            "#000000", // background color (for shadow)
                            null, // font name (no caption)
                            0.0, // font size (no caption)
                            null, // caption (no caption)
                            0.0, // rotation angle (applied later in collage)
                            0L, // max thumb size (0 = use percentage)
                            10, // 10% of original size
                            false, // don't resize after
                            false // don't set transparent background (for collage)
                    );
                    polaroidWands.add(wand);
                    Log.info("Polaroid effect applied");
                } catch (Throwable e) {
                    Log.error("ERROR: " + e.getMessage());
                    // Wand will be cleaned up by arena
                }
            }

            if (polaroidWands.isEmpty()) {
                throw new RuntimeException("No valid images could be processed");
            }

            Log.info("Created " + polaroidWands.size() + " polaroid images");
            Log.info("Composing collage...");

            // Compose collage from polaroid wands
            return composeCollageFromWands(polaroidWands, arena);

        } catch (Throwable e) {
            throw new RuntimeException("Collage generation failed", e);
        }
    }

    /**
     * Composes individual polaroid wands into a collage.
     * 
     * @param polaroidWands List of MemorySegments containing polaroid images
     * @param arena         Memory arena for allocations
     * @return Byte array containing the composed collage (PNG format)
     * @throws RuntimeException If collage composition fails
     */
    private byte[] composeCollageFromWands(List<MemorySegment> polaroidWands, Arena arena) {
        try {
            MemorySegment sequenceWand = null;
            double[] rotations = { -10.0, -10.0, 10.0, -5.0, 5.0, -8.0, 8.0, -12.0, 12.0 };

            long centerX = 0L;
            long centerY = 0L;

            for (int i = 0; i < polaroidWands.size(); i++) {
                MemorySegment polaroidWand = polaroidWands.get(i);
                double rotation = rotations[i % rotations.length];

                // Rotate the polaroid
                MemorySegment bgPixelWand = (MemorySegment) binder.getNewPixelWand().invoke();
                binder.getPixelSetColor().invoke(bgPixelWand, arena.allocateFrom("none"));

                int rotateResult = (int) binder.getMagickRotateImage().invoke(
                        polaroidWand, bgPixelWand, rotation);
                if (rotateResult == 0) {
                    Log.warn("WARNING: Failed to rotate polaroid " + (i + 1));
                }

                // Get dimensions after rotation
                long polaroidWidth = (long) binder.getMagickGetImageWidth().invoke(polaroidWand);
                long polaroidHeight = (long) binder.getMagickGetImageHeight().invoke(polaroidWand);

                // Annotate image with name and rotation degree (only in debug mode)
                if (debugAnnotations) {
                    String annotationText = String.format("polaroid_%d %.0f°", i + 1, rotation);
                    MemorySegment textStr = arena.allocateFrom(annotationText);

                    MemorySegment drawWand = (MemorySegment) binder.getNewDrawingWand().invoke();
                    binder.getDrawSetFontSize().invoke(drawWand, 24.0);

                    MemorySegment textColorWand = (MemorySegment) binder.getNewPixelWand().invoke();
                    binder.getPixelSetColor().invoke(textColorWand, arena.allocateFrom("black"));
                    binder.getDrawSetFillColor().invoke(drawWand, textColorWand);

                    int annotateResult = (int) binder.getMagickAnnotateImage().invoke(
                            polaroidWand,
                            drawWand,
                            10.0, // x offset from left
                            30.0, // y offset from top
                            0.0, // angle (0 = horizontal)
                            textStr);
                    if (annotateResult == 0) {
                        Log.warn("WARNING: Failed to annotate polaroid " + (i + 1));
                    } else {
                        Log.info("Annotated: " + annotationText);
                    }

                    binder.getDestroyDrawingWand().invoke(drawWand);
                }

                // Set page geometry with offset
                int pageResult = (int) binder.getMagickSetImagePage().invoke(
                        polaroidWand,
                        polaroidWidth,
                        polaroidHeight,
                        centerX,
                        centerY);
                if (pageResult == 0) {
                    Log.warn("WARNING: Failed to set page geometry for polaroid " + (i + 1));
                } else {
                    Log.info("Page geometry: " + polaroidWidth + "x" + polaroidHeight + " +" + centerX + "+"
                            + centerY);
                }

                Log.info("Loaded polaroid " + (i + 1) + " (" + polaroidWidth + "x" + polaroidHeight + ") at +"
                        + centerX
                        + "+" + centerY + " with rotation " + rotation);

                // Add to sequence
                if (i == 0) {
                    sequenceWand = polaroidWand;
                    Log.info("    First image - using as base sequence");
                } else {
                    int addResult = (int) binder.getMagickAddImage().invoke(sequenceWand, polaroidWand);
                    if (addResult == 0) {
                        Log.warn("WARNING: Failed to add polaroid " + (i + 1) + " to sequence");
                    } else {
                        Log.info("Added to sequence");
                    }
                }

                // Move position for next polaroid
                centerX += polaroidWidth / 2 + 5L;
            }

            // Verify we have the correct number of images in sequence
            long numImages = (long) binder.getMagickGetNumberImages().invoke(sequenceWand);
            Log.info("Sequence contains " + numImages + " images");

            // Reset iterator to ensure we're working with all images in the sequence
            binder.getMagickResetIterator().invoke(sequenceWand);

            // Merge layers
            MemorySegment mergedWand = (MemorySegment) binder.getMagickMergeImageLayers()
                    .invoke(sequenceWand, 51); // MergeLayer = 51
            if (mergedWand == MemorySegment.NULL) {
                throw new RuntimeException("Failed to merge image layers");
            }
            long mergedWidthBefore = (long) binder.getMagickGetImageWidth().invoke(mergedWand);
            long mergedHeightBefore = (long) binder.getMagickGetImageHeight().invoke(mergedWand);
            Log.info("Merged layers using MergeLayer - size before repage: " + mergedWidthBefore + "x"
                    + mergedHeightBefore);

            // Convert to sRGB colorspace
            int colorspaceResult = (int) binder.getMagickTransformImageColorspace()
                    .invoke(mergedWand, 25); // sRGBColorspace = 25
            if (colorspaceResult == 0) {
                Log.warn("WARNING: Failed to convert to sRGB");
            } else {
                Log.info("Converted to sRGB colorspace");
            }

            // Repage
            long mergedWidth = (long) binder.getMagickGetImageWidth().invoke(mergedWand);
            long mergedHeight = (long) binder.getMagickGetImageHeight().invoke(mergedWand);
            int repageResult = (int) binder.getMagickSetImagePage().invoke(
                    mergedWand, mergedWidth, mergedHeight, 0L, 0L);
            if (repageResult == 0) {
                Log.warn("WARNING: Failed to repage");
            } else {
                long finalWidth = (long) binder.getMagickGetImageWidth().invoke(mergedWand);
                long finalHeight = (long) binder.getMagickGetImageHeight().invoke(mergedWand);
                Log.info("Repaged (removed offset) - final size: " + finalWidth + "x" + finalHeight);
            }

            // Export as PNG
            binder.getMagickSetFormat().invoke(mergedWand, arena.allocateFrom("PNG"));

            MemorySegment lengthPtr = arena.allocate(ValueLayout.JAVA_LONG);
            MemorySegment resultPtr = (MemorySegment) binder.getMagickGetImageBlob()
                    .invoke(mergedWand, lengthPtr);

            if (resultPtr == MemorySegment.NULL) {
                throw new RuntimeException("Failed to get image blob");
            }

            long blobSize = lengthPtr.get(ValueLayout.JAVA_LONG, 0);
            Log.info("Exported collage: " + blobSize + " bytes");

            // Copy the blob data to a Java byte array before Arena closes
            byte[] result = resultPtr.reinterpret(blobSize).toArray(ValueLayout.JAVA_BYTE);

            // Free the ImageMagick-allocated memory
            binder.getMagickRelinquishMemory().invoke(resultPtr);

            return result;

        } catch (Throwable e) {
            throw new RuntimeException("Collage composition failed", e);
        }
    }
}
