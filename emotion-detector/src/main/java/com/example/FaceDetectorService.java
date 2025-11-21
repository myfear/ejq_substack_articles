package com.example;

import static org.bytedeco.opencv.global.opencv_core.CV_8UC1;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.logging.Logger;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FaceDetectorService {

    private static final Logger LOG = Logger.getLogger(FaceDetectorService.class.getName());
    private CascadeClassifier detector;

    @PostConstruct
    void init() throws IOException {
        LOG.info("Initializing FaceDetectorService...");

        // Load the cascade file from classpath and extract to temp file
        // OpenCV CascadeClassifier requires a filesystem path, not a classpath resource
        InputStream resourceStream = getClass().getClassLoader()
                .getResourceAsStream("haarcascade_frontalface_default.xml");
        if (resourceStream == null) {
            LOG.severe("Cascade file not found in resources!");
            throw new IllegalStateException("Cascade file not found in resources");
        }
        LOG.info("Found cascade file in resources");

        // Create temp file in target directory
        Path targetDir = Paths.get("target");
        if (!Files.exists(targetDir)) {
            Files.createDirectories(targetDir);
        }
        Path tempFile = targetDir.resolve("haarcascade_frontalface_default.xml");
        tempFile.toFile().deleteOnExit();
        Files.copy(resourceStream, tempFile, StandardCopyOption.REPLACE_EXISTING);
        resourceStream.close();
        LOG.info("Extracted cascade file to: " + tempFile.toAbsolutePath());

        detector = new CascadeClassifier(tempFile.toAbsolutePath().toString());
        if (detector.empty()) {
            LOG.severe("Failed to load cascade classifier!");
            throw new IllegalStateException("Failed to load cascade classifier");
        }
        LOG.info("Cascade classifier loaded successfully");
    }

    public FaceDetectionResult detectAndAnnotateFace(byte[] jpeg) {
        LOG.info("Detecting and annotating face from " + jpeg.length + " byte JPEG");

        try {
            // Create a Mat from the JPEG byte array for decoding
            BytePointer encodedData = new BytePointer(jpeg);
            Mat encodedMat = new Mat(1, jpeg.length, CV_8UC1, encodedData);
            Mat original = opencv_imgcodecs.imdecode(encodedMat, opencv_imgcodecs.IMREAD_COLOR);

            if (original == null || original.empty()) {
                LOG.warning("Failed to decode JPEG image");
                return null;
            }
            LOG.info("Decoded image: " + original.cols() + "x" + original.rows());

            Mat gray = new Mat();
            opencv_imgproc.cvtColor(original, gray, opencv_imgproc.COLOR_BGR2GRAY);
            LOG.info("Converted to grayscale");

            RectVector faces = new RectVector();
            detector.detectMultiScale(gray, faces);
            LOG.info("Face detection found " + faces.size() + " face(s)");

            if (faces.size() == 0) {
                LOG.info("No faces detected");
                return null;
            }

            Rect rect = faces.get(0);
            int x = rect.x();
            int y = rect.y();
            int width = rect.width();
            int height = rect.height();
            LOG.info("Extracting face at: x=" + x + ", y=" + y + ", w=" + width + ", h=" + height);

            // Calculate approximate confidence based on face size relative to image
            double imageArea = original.cols() * original.rows();
            double faceArea = width * height;
            double confidence = Math.min(100.0, (faceArea / imageArea) * 500.0); // Heuristic confidence

            // Draw bounding box on original image
            Mat annotated = original.clone();
            Point pt1 = new Point(x, y);
            Point pt2 = new Point(x + width, y + height);
            Scalar color = new Scalar(0, 255, 0, 0); // Green color (BGR)
            int thickness = 2;
            opencv_imgproc.rectangle(annotated, pt1, pt2, color, thickness, opencv_imgproc.LINE_8, 0);

            // Add text with confidence
            String label = String.format("Face: %.1f%%", confidence);
            Point textOrg = new Point(x, y - 10);
            if (textOrg.y() < 0) {
                textOrg.y(y + height + 20);
            }
            Scalar textColor = new Scalar(0, 255, 0, 0); // Green text
            double fontScale = 0.6;
            int fontFace = opencv_imgproc.FONT_HERSHEY_SIMPLEX;
            opencv_imgproc.putText(annotated, label, textOrg, fontFace, fontScale, textColor, 2, opencv_imgproc.LINE_AA,
                    false);

            LOG.info("Drew bounding box and label: " + label);

            // Expand bounding box for emotion analysis (include more context)
            // Use 50% padding on all sides for better emotion detection
            double expansionFactor = 0.5;
            int expandedX = Math.max(0, (int) (x - width * expansionFactor));
            int expandedY = Math.max(0, (int) (y - height * expansionFactor));
            int expandedWidth = Math.min(original.cols() - expandedX, (int) (width * (1 + 2 * expansionFactor)));
            int expandedHeight = Math.min(original.rows() - expandedY, (int) (height * (1 + 2 * expansionFactor)));

            Rect expandedRect = new Rect(expandedX, expandedY, expandedWidth, expandedHeight);
            LOG.info("Expanded face region for analysis: x=" + expandedX + ", y=" + expandedY +
                    ", w=" + expandedWidth + ", h=" + expandedHeight);

            // Extract expanded face region for emotion analysis
            Mat face = new Mat(original, expandedRect);

            // Encode annotated image
            BytePointer annotatedBuffer = new BytePointer();
            opencv_imgcodecs.imencode(".jpg", annotated, annotatedBuffer);
            byte[] annotatedImage = new byte[(int) annotatedBuffer.limit()];
            annotatedBuffer.get(annotatedImage);

            // Encode face image
            BytePointer faceBuffer = new BytePointer();
            opencv_imgcodecs.imencode(".jpg", face, faceBuffer);
            byte[] faceImage = new byte[(int) faceBuffer.limit()];
            faceBuffer.get(faceImage);

            LOG.info("Encoded annotated image to " + annotatedImage.length + " bytes, face to " + faceImage.length
                    + " bytes");

            return new FaceDetectionResult(annotatedImage, faceImage, x, y, width, height, confidence);
        } catch (Exception e) {
            LOG.severe("Error in detectAndAnnotateFace: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}