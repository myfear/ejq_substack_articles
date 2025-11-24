package com.example.vision;

import static org.bytedeco.opencv.global.opencv_imgproc.FONT_HERSHEY_SIMPLEX;
import static org.bytedeco.opencv.global.opencv_imgproc.putText;
import static org.bytedeco.opencv.global.opencv_imgproc.rectangle;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

import javax.imageio.ImageIO;

import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Scalar;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Multi;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class WebcamService {

    @Inject
    ObjectDetector detector;

    private FrameGrabber grabber;
    private boolean running = false;

    // Holds the latest detections (updated asynchronously)
    private final AtomicReference<List<ObjectDetector.Detection>> latestDetections = new AtomicReference<>(
            Collections.emptyList());

    public Multi<byte[]> stream() {
        Executor workerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "webcam-worker");
            t.setDaemon(true);
            return t;
        });

        return Multi.createFrom().emitter(emitter -> {
            workerExecutor.execute(() -> {
                OpenCVFrameConverter.ToMat converter = null;
                Java2DFrameConverter java2dConverter = null;
                try {
                    Log.info("Starting webcam stream...");
                    // 0 = Default Camera (adjust if you have multiple)
                    // Use OpenCVFrameGrabber explicitly to avoid trying unsupported grabbers
                    grabber = new OpenCVFrameGrabber(0);
                    grabber.start();
                    running = true;
                    Log.info("Webcam started successfully");

                    converter = new OpenCVFrameConverter.ToMat();
                    java2dConverter = new Java2DFrameConverter();

                    long frameCount = 0;

                    while (running && !emitter.isCancelled()) {
                        Frame capturedFrame = grabber.grab();
                        if (capturedFrame == null) {
                            try {
                                Thread.sleep(33); // ~30 FPS
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                break;
                            }
                            continue;
                        }

                        Mat mat = converter.convert(capturedFrame);
                        if (mat == null || mat.empty()) {
                            continue;
                        }

                        // --- SMART PIPELINE ---
                        // Run detection only every 5th frame to maintain high FPS
                        // Run detection asynchronously on a virtual thread to avoid blocking frame
                        // grabbing
                        if (frameCount % 5 == 0) {
                            // Clone mat because detection might take time and mat is reused by grabber
                            Mat detectionInput = mat.clone();
                            Thread.ofVirtual().start(() -> {
                                try {
                                    List<ObjectDetector.Detection> dets = detector.detect(detectionInput);
                                    latestDetections.set(dets);
                                } finally {
                                    detectionInput.close();
                                }
                            });
                        }

                        // --- ANNOTATION ---
                        // Always draw the LATEST available detections
                        List<ObjectDetector.Detection> currentDetections = latestDetections.get();
                        for (ObjectDetector.Detection d : currentDetections) {
                            // Draw Box (Green)
                            rectangle(mat, new Point(d.x(), d.y()), new Point(d.x2(), d.y2()),
                                    new Scalar(0, 255, 0, 0), 4, 8, 0);

                            // Draw Label
                            String labelText = String.format("%s %.0f%%", d.label(), d.confidence() * 100);
                            putText(mat, labelText, new Point(d.x(), d.y() - 10),
                                    FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(0, 255, 0, 0), 2, 8, false);
                        }

                        // --- CONVERT ANNOTATED MAT TO JPEG ---
                        // Convert Mat back to Frame, then to BufferedImage
                        Frame annotatedFrame = converter.convert(mat);
                        BufferedImage bufferedImage = java2dConverter.getBufferedImage(annotatedFrame);

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        ImageIO.write(bufferedImage, "jpg", baos);
                        byte[] imageBytes = baos.toByteArray();

                        emitter.emit(imageBytes);
                        frameCount++;

                        if (frameCount % 30 == 0) {
                            Log.infof("Frames emitted: %d", frameCount);
                        }
                    }

                    Log.info("Stopping webcam stream...");
                    grabber.stop();
                    grabber.release();
                    emitter.complete();

                } catch (Exception e) {
                    Log.errorf(e, "Error in webcam stream: %s", e.getMessage());
                    emitter.fail(e);
                } finally {
                    if (converter != null) {
                        try {
                            converter.close();
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    }
                    if (java2dConverter != null) {
                        try {
                            java2dConverter.close();
                        } catch (Exception e) {
                            // Ignore cleanup errors
                        }
                    }
                }
            });
        });
    }

    @PreDestroy
    void cleanup() {
        running = false;
        try {
            if (grabber != null)
                grabber.release();
        } catch (Exception e) {
            Log.errorf(e, "Error releasing webcam grabber");
        }
    }
}