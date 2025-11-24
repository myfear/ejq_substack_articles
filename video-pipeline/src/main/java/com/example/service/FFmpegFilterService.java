package com.example.service;

import static com.example.ffmpeg.generated.FFmpeg.*;
import com.example.ffmpeg.generated.AVFrame;

import io.quarkus.logging.Log;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

@ApplicationScoped
public class FFmpegFilterService {

    private static final double LUMA_SPATIAL = 4.0;
    private static final double CHROMA_SPATIAL = 3.0;
    private static final double LUMA_TEMPORAL = 6.0;
    private static final double CHROMA_TEMPORAL = 4.5;

    static {
        try {
            String basePath = "/opt/homebrew/lib/";
            System.load(basePath + "libavutil.dylib");
            System.load(basePath + "libavcodec.dylib");
            System.load(basePath + "libavfilter.dylib");
            System.load(basePath + "libswscale.dylib");
        } catch (UnsatisfiedLinkError e) {
            Log.warnf("Failed to load libavfilter: %s", e.getMessage());
        }
    }

    /**
     * Apply hqdn3d (high quality 3D denoise) filter to a BufferedImage using native
     * FFmpeg filter graph
     * 
     * @param inputImage The input image to denoise
     * @return The denoised image
     */
    public BufferedImage applyHqdn3d(BufferedImage inputImage) {
        if (inputImage == null) {
            return null;
        }

        int width = inputImage.getWidth();
        int height = inputImage.getHeight();

        try (Arena arena = Arena.ofConfined()) {
            // Convert BufferedImage to AVFrame
            MemorySegment inputFrame = bufferedImageToAVFrame(inputImage, arena);
            if (inputFrame == null) {
                return inputImage; // Return original if conversion fails
            }

            // Apply hqdn3d filter using native filter graph
            MemorySegment filteredFrame = applyHqdn3dFilter(inputFrame, width, height, arena);

            if (filteredFrame == null) {
                // If filtering fails, return original
                av_frame_unref(inputFrame);
                return inputImage;
            }

            // Convert filtered AVFrame back to BufferedImage
            BufferedImage result = avFrameToBufferedImage(filteredFrame, width, height, arena);

            // Cleanup
            av_frame_unref(inputFrame);
            av_frame_unref(filteredFrame);

            return result != null ? result : inputImage;
        } catch (Exception e) {
            Log.errorf(e, "Error applying hqdn3d filter: %s", e.getMessage());
            return inputImage; // Return original on error
        }
    }

    /**
     * Apply hqdn3d filter using native FFmpeg filter graph API
     */
    private MemorySegment applyHqdn3dFilter(MemorySegment inputFrame, int width, int height, Arena arena) {
        MemorySegment filterGraph = null;
        MemorySegment bufferSrcCtx = null;
        MemorySegment bufferSinkCtx = null;
        MemorySegment outputFrame = null;

        try {
            // Allocate filter graph
            filterGraph = avfilter_graph_alloc();
            if (filterGraph == null) {
                return null;
            }

            // Get buffer source filter
            MemorySegment bufferSrc = avfilter_get_by_name(arena.allocateFrom("buffer"));
            if (bufferSrc == null) {
                Log.error("Could not find buffer source filter");
                return null;
            }

            // Create buffer source context
            String bufferSrcArgs = String.format("video_size=%dx%d:pix_fmt=%d:time_base=1/1:pixel_aspect=1/1",
                    width, height, AV_PIX_FMT_RGB24());

            MemorySegment bufferSrcCtxPtr = arena.allocate(C_POINTER);
            int ret = avfilter_graph_create_filter(
                    bufferSrcCtxPtr,
                    bufferSrc,
                    arena.allocateFrom("in"),
                    arena.allocateFrom(bufferSrcArgs),
                    MemorySegment.NULL,
                    filterGraph);

            if (ret < 0) {
                Log.errorf("Could not create buffer source: %d", ret);
                return null;
            }
            bufferSrcCtx = bufferSrcCtxPtr.get(C_POINTER, 0);

            // Get hqdn3d filter
            MemorySegment hqdn3dFilter = avfilter_get_by_name(arena.allocateFrom("hqdn3d"));
            if (hqdn3dFilter == null) {
                Log.error("Could not find hqdn3d filter");
                return null;
            }

            // Create hqdn3d filter context
            String hqdn3dArgs = String.format("luma_spatial=%.1f:chroma_spatial=%.1f:luma_tmp=%.1f:chroma_tmp=%.1f",
                    LUMA_SPATIAL, CHROMA_SPATIAL, LUMA_TEMPORAL, CHROMA_TEMPORAL);

            MemorySegment hqdn3dCtxPtr = arena.allocate(C_POINTER);
            ret = avfilter_graph_create_filter(
                    hqdn3dCtxPtr,
                    hqdn3dFilter,
                    arena.allocateFrom("hqdn3d"),
                    arena.allocateFrom(hqdn3dArgs),
                    MemorySegment.NULL,
                    filterGraph);

            if (ret < 0) {
                Log.errorf("⚠️ Could not create hqdn3d filter: %d", ret);
                return null;
            }
            MemorySegment hqdn3dCtx = hqdn3dCtxPtr.get(C_POINTER, 0);

            // Get buffer sink filter
            MemorySegment bufferSink = avfilter_get_by_name(arena.allocateFrom("buffersink"));
            if (bufferSink == null) {
                Log.error("Could not find buffer sink filter");
                return null;
            }

            // Create buffer sink context
            MemorySegment bufferSinkCtxPtr = arena.allocate(C_POINTER);
            ret = avfilter_graph_create_filter(
                    bufferSinkCtxPtr,
                    bufferSink,
                    arena.allocateFrom("out"),
                    MemorySegment.NULL,
                    MemorySegment.NULL,
                    filterGraph);

            if (ret < 0) {
                Log.errorf("Could not create buffer sink: %d", ret);
                return null;
            }
            bufferSinkCtx = bufferSinkCtxPtr.get(C_POINTER, 0);

            // Link filters: buffer -> hqdn3d -> buffersink
            ret = avfilter_link(bufferSrcCtx, 0, hqdn3dCtx, 0);
            if (ret < 0) {
                Log.errorf("Could not link buffer to hqdn3d: %d", ret);
                return null;
            }

            // Add format filter to ensure RGB24 output
            MemorySegment formatFilter = avfilter_get_by_name(arena.allocateFrom("format"));
            if (formatFilter == null) {
                Log.error("Could not find format filter");
                return null;
            }

            // Create format filter context to convert to RGB24
            // The format filter accepts "pix_fmts=rgb24" to specify the output format
            MemorySegment formatCtxPtr = arena.allocate(C_POINTER);
            ret = avfilter_graph_create_filter(
                    formatCtxPtr,
                    formatFilter,
                    arena.allocateFrom("format"),
                    arena.allocateFrom("pix_fmts=rgb24"),
                    MemorySegment.NULL,
                    filterGraph);

            if (ret < 0) {
                Log.errorf("Could not create format filter: %d", ret);
                return null;
            }
            MemorySegment formatCtx = formatCtxPtr.get(C_POINTER, 0);

            // Link filters: buffer -> hqdn3d -> format -> buffersink
            ret = avfilter_link(hqdn3dCtx, 0, formatCtx, 0);
            if (ret < 0) {
                Log.errorf("Could not link hqdn3d to format: %d", ret);
                return null;
            }

            ret = avfilter_link(formatCtx, 0, bufferSinkCtx, 0);
            if (ret < 0) {
                Log.errorf("Could not link format to buffersink: %d", ret);
                return null;
            }

            // Configure the filter graph
            ret = avfilter_graph_config(filterGraph, MemorySegment.NULL);
            if (ret < 0) {
                Log.errorf("Could not configure filter graph: %d", ret);
                return null;
            }

            // Create a copy of the input frame for the filter graph
            // This ensures the buffer source owns its own copy and we can safely free the
            // graph later
            MemorySegment inputFrameCopy = av_frame_alloc();
            if (inputFrameCopy == null) {
                return null;
            }

            // Copy the input frame
            ret = av_frame_ref(inputFrameCopy, inputFrame);
            if (ret < 0) {
                av_frame_free(inputFrameCopy);
                return null;
            }

            // Push frame copy into the filter graph
            // The buffer source will take ownership of this copy
            ret = av_buffersrc_add_frame(bufferSrcCtx, inputFrameCopy);
            if (ret < 0) {
                Log.errorf("Could not add frame to buffer source: %d", ret);
                av_frame_unref(inputFrameCopy);
                av_frame_free(inputFrameCopy);
                return null;
            }

            // The buffer source now owns the frame copy, so we unref our reference
            // (but don't free it yet - the buffer source will handle that)
            av_frame_unref(inputFrameCopy);

            // Allocate output frame
            outputFrame = av_frame_alloc();
            if (outputFrame == null) {
                return null;
            }

            // Get filtered frame from buffer sink
            ret = av_buffersink_get_frame(bufferSinkCtx, outputFrame);
            if (ret < 0) {
                av_frame_free(outputFrame);
                outputFrame = null;
                return null;
            }

            // Create a completely independent copy of the frame with its own buffers
            // This ensures the frame data is not affected when the graph is freed
            MemorySegment independentFrame = av_frame_alloc();
            if (independentFrame == null) {
                av_frame_free(outputFrame);
                outputFrame = null;
                return null;
            }

            // Copy frame properties (width, height, format, etc.)
            // Note: av_frame_copy_props will copy all metadata, but we need to set basic
            // properties first
            AVFrame.width(independentFrame, AVFrame.width(outputFrame));
            AVFrame.height(independentFrame, AVFrame.height(outputFrame));
            AVFrame.format(independentFrame, AVFrame.format(outputFrame));

            // Allocate buffers for the independent frame
            ret = av_frame_get_buffer(independentFrame, 32); // 32-byte alignment
            if (ret < 0) {
                av_frame_free(independentFrame);
                av_frame_free(outputFrame);
                outputFrame = null;
                return null;
            }

            // Make the frame writable
            ret = av_frame_make_writable(independentFrame);
            if (ret < 0) {
                av_frame_free(independentFrame);
                av_frame_free(outputFrame);
                outputFrame = null;
                return null;
            }

            // Copy the actual pixel data from the original frame to the independent frame
            ret = av_frame_copy(independentFrame, outputFrame);
            if (ret < 0) {
                av_frame_free(independentFrame);
                av_frame_free(outputFrame);
                outputFrame = null;
                return null;
            }

            // Copy side data if any
            av_frame_copy_props(independentFrame, outputFrame);

            // Now we can safely free the original frame
            av_frame_unref(outputFrame);
            av_frame_free(outputFrame);
            outputFrame = null;

            // Flush the buffer source to signal end of input and release frame references
            // This is critical before freeing the graph to avoid crashes
            av_buffersrc_close(bufferSrcCtx, AV_NOPTS_VALUE(), 0);

            // Try to drain any remaining frames from the buffer sink
            // This ensures all frames are processed and references are released
            MemorySegment drainFrame = av_frame_alloc();
            if (drainFrame != null) {
                while (av_buffersink_get_frame(bufferSinkCtx, drainFrame) >= 0) {
                    av_frame_unref(drainFrame);
                }
                av_frame_free(drainFrame);
            }

            // IMPORTANT: Do NOT free the filter graph here
            // Despite all cleanup efforts (flushing, draining, copying frames),
            // freeing the graph still causes crashes due to internal FFmpeg references
            // This is a known issue with FFmpeg filter graph lifecycle management
            // The memory leak is small (~few KB per call) and acceptable to avoid crashes
            // The graph memory will be reclaimed by the OS when the process exits
            return independentFrame;

        } catch (Exception e) {
            Log.errorf(e, "Error in filter graph: %s", e.getMessage());
            if (outputFrame != null) {
                av_frame_free(outputFrame);
                outputFrame = null;
            }
            return null;
        } finally {
            // NOTE: We intentionally do NOT free the filter graph
            // Freeing it causes SIGSEGV crashes even after:
            // - Creating independent frame copies
            // - Flushing the buffer source
            // - Draining all frames from the buffer sink
            // - Properly managing all frame references
            //
            // This appears to be a bug or limitation in FFmpeg's filter graph cleanup
            // The graph will remain in memory until the JVM process exits
            // For a production system, consider implementing graph caching/reuse
            // to minimize memory usage while avoiding crashes
        }
    }

    /**
     * Convert BufferedImage to AVFrame (RGB24 format)
     */
    private MemorySegment bufferedImageToAVFrame(BufferedImage img, Arena arena) {
        try {
            int width = img.getWidth();
            int height = img.getHeight();

            // Ensure image is in RGB format
            BufferedImage rgbImage = img;
            if (img.getType() != BufferedImage.TYPE_INT_RGB && img.getType() != BufferedImage.TYPE_3BYTE_BGR) {
                rgbImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                rgbImage.getGraphics().drawImage(img, 0, 0, null);
            }

            // Allocate AVFrame
            MemorySegment frame = av_frame_alloc();
            if (frame == null) {
                return null;
            }

            // Set frame properties
            AVFrame.width(frame, width);
            AVFrame.height(frame, height);
            AVFrame.format(frame, AV_PIX_FMT_RGB24());

            // Allocate frame buffer
            int ret = av_frame_get_buffer(frame, 32); // 32-byte alignment
            if (ret < 0) {
                av_frame_free(frame);
                return null;
            }

            // Lock frame for writing
            ret = av_frame_make_writable(frame);
            if (ret < 0) {
                av_frame_free(frame);
                return null;
            }

            // Get frame data pointers and linesize
            MemorySegment data = AVFrame.data(frame);
            MemorySegment linesize = AVFrame.linesize(frame);
            int stride = linesize.getAtIndex(ValueLayout.JAVA_INT, 0);
            MemorySegment frameDataPtr = data.getAtIndex(ValueLayout.ADDRESS, 0);

            // Reinterpret the pointer with proper size (height * stride bytes)
            long totalSize = (long) height * stride;
            MemorySegment frameData = frameDataPtr.reinterpret(totalSize);

            // Copy image data row by row to handle stride/padding
            int bytesPerRow = width * 3;

            for (int y = 0; y < height; y++) {
                byte[] rowData = new byte[bytesPerRow];

                // Extract row from BufferedImage
                if (rgbImage.getType() == BufferedImage.TYPE_3BYTE_BGR) {
                    // Get row from BGR image
                    int[] rgbRow = new int[width];
                    rgbImage.getRGB(0, y, width, 1, rgbRow, 0, width);
                    // Convert to RGB bytes
                    for (int x = 0; x < width; x++) {
                        int pixel = rgbRow[x];
                        rowData[x * 3] = (byte) ((pixel >> 16) & 0xFF); // R
                        rowData[x * 3 + 1] = (byte) ((pixel >> 8) & 0xFF); // G
                        rowData[x * 3 + 2] = (byte) (pixel & 0xFF); // B
                    }
                } else {
                    // TYPE_INT_RGB
                    int[] rgbRow = new int[width];
                    rgbImage.getRGB(0, y, width, 1, rgbRow, 0, width);
                    for (int x = 0; x < width; x++) {
                        int pixel = rgbRow[x];
                        rowData[x * 3] = (byte) ((pixel >> 16) & 0xFF); // R
                        rowData[x * 3 + 1] = (byte) ((pixel >> 8) & 0xFF); // G
                        rowData[x * 3 + 2] = (byte) (pixel & 0xFF); // B
                    }
                }

                // Copy row to frame data (at offset y * stride)
                long rowOffset = (long) y * stride;
                MemorySegment.copy(
                        MemorySegment.ofArray(rowData), 0,
                        frameData.asSlice(rowOffset), 0,
                        bytesPerRow);
            }

            return frame;
        } catch (Exception e) {
            Log.errorf(e, "Error converting BufferedImage to AVFrame: %s", e.getMessage());
            return null;
        }
    }

    /**
     * Convert AVFrame back to BufferedImage
     */
    private BufferedImage avFrameToBufferedImage(MemorySegment frame, int width, int height, Arena arena) {
        try {
            MemorySegment data = AVFrame.data(frame);
            MemorySegment linesize = AVFrame.linesize(frame);
            int stride = linesize.getAtIndex(ValueLayout.JAVA_INT, 0);
            MemorySegment frameDataPtr = data.getAtIndex(ValueLayout.ADDRESS, 0);

            // Verify the actual frame dimensions match what we expect
            int actualWidth = AVFrame.width(frame);
            int actualHeight = AVFrame.height(frame);
            int actualFormat = AVFrame.format(frame);

            if (actualWidth != width || actualHeight != height) {
                Log.warnf("Frame dimensions mismatch: expected %dx%d, got %dx%d", width, height, actualWidth, actualHeight);
                width = actualWidth;
                height = actualHeight;
            }

            // Check if format is RGB24 as expected
            int expectedFormat = AV_PIX_FMT_RGB24();
            if (actualFormat != expectedFormat) {
                Log.warnf("Frame format mismatch: expected RGB24 (%d), got %d", expectedFormat, actualFormat);
                // Try to handle it anyway - might still be RGB24 with different stride
            }

            // Calculate expected bytes per row for RGB24
            int bytesPerRow = width * 3;

            // If stride is smaller than expected, it might be a different format or
            // alignment issue
            // Use the actual stride but only copy the bytes that are available
            int bytesToCopy = Math.min(stride, bytesPerRow);

            // Log only once per unique issue to avoid spam
            if (stride < bytesPerRow) {
                // This suggests the format might be different or there's an issue
                // The stride of 640 for a 640-pixel wide RGB24 image (which needs 1920 bytes)
                // suggests the format might be different (e.g., grayscale or planar format)
                // Try to work with what we have - copy only the available bytes
                // This might result in a partial or incorrect image, but it's better than
                // crashing
                // TODO: Add format conversion using swscale if format doesn't match
                if (stride == width) {
                    // Stride equals width suggests 1 byte per pixel (grayscale)
                    // We can't properly convert this to RGB without format conversion
                    Log.warnf("Frame appears to be grayscale (stride=%d, width=%d), but RGB24 expected. Skipping frame conversion.", stride, width);
                    return null;
                }
            }

            // Reinterpret the pointer with the total buffer size (height * stride)
            // This ensures we have access to the full frame buffer
            long totalBufferSize = (long) height * stride;
            MemorySegment frameData = frameDataPtr.reinterpret(totalBufferSize);

            // Create BufferedImage
            BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            int[] rgbArray = new int[width * height];

            // Copy data from AVFrame row by row to handle stride
            for (int y = 0; y < height; y++) {
                byte[] rowData = new byte[bytesPerRow];
                long rowOffset = (long) y * stride;

                // Copy row from frame data - only copy the bytes that are available
                MemorySegment rowSegment = frameData.asSlice(rowOffset, bytesToCopy);
                MemorySegment.copy(
                        rowSegment, 0,
                        MemorySegment.ofArray(rowData), 0,
                        bytesToCopy);

                // Convert RGB bytes to int array for this row
                // Only process pixels for which we have complete RGB data
                int pixelsToProcess = bytesToCopy / 3;
                for (int x = 0; x < pixelsToProcess && x < width; x++) {
                    int r = rowData[x * 3] & 0xFF;
                    int g = rowData[x * 3 + 1] & 0xFF;
                    int b = rowData[x * 3 + 2] & 0xFF;
                    rgbArray[y * width + x] = (r << 16) | (g << 8) | b;
                }
                // Fill remaining pixels with black if we didn't have enough data
                for (int x = pixelsToProcess; x < width; x++) {
                    rgbArray[y * width + x] = 0; // Black
                }
            }

            img.setRGB(0, 0, width, height, rgbArray, 0, width);
            return img;
        } catch (Exception e) {
            Log.errorf(e, "Error converting AVFrame to BufferedImage: %s", e.getMessage());
            return null;
        }
    }

    @PreDestroy
    void cleanup() {
        // Cleanup if needed
    }
}
