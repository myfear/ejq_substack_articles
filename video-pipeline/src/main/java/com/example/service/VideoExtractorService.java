package com.example.service;

import static com.example.ffmpeg.generated.FFmpeg.*;
import com.example.ffmpeg.generated.AVCodecContext;
import com.example.ffmpeg.generated.AVCodecParameters;
import com.example.ffmpeg.generated.AVFormatContext;
import com.example.ffmpeg.generated.AVStream;
import com.example.ffmpeg.generated.AVPacket;
import com.example.ffmpeg.generated.AVFrame;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.lang.foreign.*;
import java.nio.file.Path;
import java.util.function.BiConsumer;

@ApplicationScoped
public class VideoExtractorService {

    private static final int VIDEO_STREAM_INDEX = 0;
    private static final int BGR_BYTES_PER_PIXEL = 3;
    private static final int MAX_PLANES = 4;

    static {
        try {
            String basePath = "/opt/homebrew/lib/";
            System.load(basePath + "libavutil.dylib");
            System.load(basePath + "libswscale.dylib");
            System.load(basePath + "libavcodec.dylib");
            System.load(basePath + "libavformat.dylib");
            Log.info("FFmpeg libraries loaded successfully");
        } catch (UnsatisfiedLinkError e) {
            Log.warn("Failed to load FFmpeg libraries: " + e.getMessage());
        }
    }

    @Inject
    ImageProcessorService imageProcessor;

    public void extractAndProcess(Path videoPath, BiConsumer<byte[], Integer> frameConsumer) {
        Log.infof("Starting video extraction and processing: %s", videoPath);
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment formatCtxPtr = arena.allocate(C_POINTER);
            MemorySegment formatCtx = MemorySegment.NULL;
            MemorySegment codecCtx = MemorySegment.NULL;
            MemorySegment swsCtx = MemorySegment.NULL;
            MemorySegment packet = MemorySegment.NULL;
            MemorySegment frame = MemorySegment.NULL;

            try {
                VideoContext videoCtx = initializeVideoContext(videoPath, arena, formatCtxPtr);
                formatCtx = videoCtx.formatCtx();
                codecCtx = videoCtx.codecCtx();
                Log.debugf("Video initialized: %dx%d", videoCtx.width(), videoCtx.height());

                FrameConverter converter = setupFrameConverter(videoCtx, arena);
                swsCtx = converter.swsCtx();

                packet = av_packet_alloc();
                frame = av_frame_alloc();

                processVideoFrames(formatCtx, codecCtx, frame, packet, converter, frameConsumer);
                Log.info("Video processing completed successfully");
            } catch (Exception e) {
                Log.errorf(e, "Error processing video: %s", videoPath);
                throw e;
            } finally {
                cleanupResources(packet, frame, swsCtx, codecCtx, formatCtx, formatCtxPtr);
            }
        }
    }

    private VideoContext initializeVideoContext(Path videoPath, Arena arena, MemorySegment formatCtxPtr) {
        if (avformat_open_input(formatCtxPtr, arena.allocateFrom(videoPath.toString()), MemorySegment.NULL,
                MemorySegment.NULL) < 0) {
            throw new RuntimeException("Failed to open video: " + videoPath);
        }

        MemorySegment formatCtx = formatCtxPtr.get(C_POINTER, 0);
        avformat_find_stream_info(formatCtx, MemorySegment.NULL);

        MemorySegment streams = AVFormatContext.streams(formatCtx);
        MemorySegment stream = streams.get(C_POINTER, VIDEO_STREAM_INDEX);
        MemorySegment codecParams = AVStream.codecpar(stream);

        MemorySegment decoder = avcodec_find_decoder(AVCodecParameters.codec_id(codecParams));
        MemorySegment codecCtx = avcodec_alloc_context3(decoder);
        avcodec_parameters_to_context(codecCtx, codecParams);
        
        if (avcodec_open2(codecCtx, decoder, MemorySegment.NULL) < 0) {
            throw new RuntimeException("Failed to open codec for video: " + videoPath);
        }

        int width = AVCodecContext.width(codecCtx);
        int height = AVCodecContext.height(codecCtx);
        int pixelFormat = AVCodecContext.pix_fmt(codecCtx);

        return new VideoContext(formatCtx, codecCtx, width, height, pixelFormat);
    }

    private FrameConverter setupFrameConverter(VideoContext videoCtx, Arena arena) {
        // Use BGR24 (Java's native format) - Blue-Green-Red, 3 bytes per pixel
        // This ensures colors are correct in Java 2D
        int destPixelFormat = AV_PIX_FMT_BGR24();

        MemorySegment swsCtx = sws_getContext(
                videoCtx.width(), videoCtx.height(), videoCtx.pixelFormat(),
                videoCtx.width(), videoCtx.height(), destPixelFormat,
                SWS_BILINEAR(), MemorySegment.NULL, MemorySegment.NULL, MemorySegment.NULL);

        long bufferSize = (long) videoCtx.width() * videoCtx.height() * BGR_BYTES_PER_PIXEL;
        MemorySegment bgrBuffer = arena.allocate(bufferSize);

        MemorySegment destLinesize = arena.allocate(C_INT, MAX_PLANES);
        destLinesize.setAtIndex(C_INT, 0, videoCtx.width() * BGR_BYTES_PER_PIXEL);

        MemorySegment destData = arena.allocate(C_POINTER, MAX_PLANES);
        destData.setAtIndex(C_POINTER, 0, bgrBuffer);

        return new FrameConverter(swsCtx, bgrBuffer, destData, destLinesize,
                videoCtx.width(), videoCtx.height());
    }

    private void processVideoFrames(MemorySegment formatCtx, MemorySegment codecCtx,
                                    MemorySegment frame, MemorySegment packet,
                                    FrameConverter converter, BiConsumer<byte[], Integer> frameConsumer) {
        int frameIdx = 0;

        while (av_read_frame(formatCtx, packet) >= 0) {
            if (AVPacket.stream_index(packet) == VIDEO_STREAM_INDEX) {
                avcodec_send_packet(codecCtx, packet);
                
                while (avcodec_receive_frame(codecCtx, frame) == 0) {
                    sws_scale(converter.swsCtx(), AVFrame.data(frame), AVFrame.linesize(frame),
                            0, converter.height(), converter.destData(), converter.destLinesize());

                    byte[] jpg = imageProcessor.overlayLogo(converter.bgrBuffer(),
                            converter.width(), converter.height());
                    frameConsumer.accept(jpg, frameIdx++);
                    
                    if (frameIdx % 100 == 0) {
                        Log.debugf("Processed %d frames", frameIdx);
                    }
                }
            }
            av_packet_unref(packet);
        }
        Log.infof("Total frames processed: %d", frameIdx);
    }

    private void cleanupResources(MemorySegment packet, MemorySegment frame, MemorySegment swsCtx,
                                 MemorySegment codecCtx, MemorySegment formatCtx,
                                 MemorySegment formatCtxPtr) {
        if (!packet.equals(MemorySegment.NULL)) {
            av_packet_free(packet);
        }
        if (!frame.equals(MemorySegment.NULL)) {
            av_frame_free(frame);
        }
        if (!swsCtx.equals(MemorySegment.NULL)) {
            sws_freeContext(swsCtx);
        }
        if (!codecCtx.equals(MemorySegment.NULL)) {
            avcodec_free_context(codecCtx);
        }
        if (!formatCtx.equals(MemorySegment.NULL)) {
            avformat_close_input(formatCtxPtr);
        }
    }

    private record VideoContext(MemorySegment formatCtx, MemorySegment codecCtx,
                               int width, int height, int pixelFormat) {}

    private record FrameConverter(MemorySegment swsCtx, MemorySegment bgrBuffer,
                                 MemorySegment destData, MemorySegment destLinesize,
                                 int width, int height) {}
}