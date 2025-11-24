package com.example.vision;

import static org.bytedeco.opencv.global.opencv_core.CV_32F;
import static org.bytedeco.opencv.global.opencv_dnn.DNN_BACKEND_DEFAULT;
import static org.bytedeco.opencv.global.opencv_dnn.DNN_TARGET_CPU;
import static org.bytedeco.opencv.global.opencv_dnn.blobFromImage;
import static org.bytedeco.opencv.global.opencv_dnn.readNetFromCaffe;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.bytedeco.javacpp.indexer.FloatIndexer;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_dnn.Net;

import io.quarkus.logging.Log;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ObjectDetector {

    private Net net;
    // MobileNet-SSD Class Labels (Index matches output)
    private static final String[] CLASS_NAMES = {
            "background", "aeroplane", "bicycle", "bird", "boat",
            "bottle", "bus", "car", "cat", "chair", "cow", "diningtable",
            "dog", "horse", "motorbike", "person", "pottedplant",
            "sheep", "sofa", "train", "tvmonitor"
    };

    @PostConstruct
    void init() {
        try {
            // Load model files from resources
            String protoPath = getResourcePath("models/file-mobilenetssd_deploy.prototxt");
            String weightsPath = getResourcePath("models/MobileNetSSD_deploy.caffemodel");

            Log.info("Loading Neural Network...");
            net = readNetFromCaffe(protoPath, weightsPath);

            if (net.empty()) {
                throw new RuntimeException("Failed to load Neural Network!");
            }

            // Set backend to default (CPU).
            // If you have CUDA, you can switch this to DNN_BACKEND_CUDA
            net.setPreferableBackend(DNN_BACKEND_DEFAULT);
            net.setPreferableTarget(DNN_TARGET_CPU);

            Log.info("Model Loaded Successfully!");
        } catch (Exception e) {
            throw new RuntimeException("Error initializing model", e);
        }
    }

    public List<Detection> detect(Mat frame) {
        List<Detection> results = new ArrayList<>();

        // 1. Prepare input blob
        // MobileNet-SSD expects 300x300 resolution
        // Scale factor: 0.007843 (1/127.5)
        Mat blob = blobFromImage(frame, 0.007843, new Size(300, 300),
                new Scalar(127.5, 127.5, 127.5, 0), false, false, CV_32F);

        // 2. Forward Pass (Inference)
        net.setInput(blob);
        Mat output = net.forward(); // This is the CPU-intensive part

        // 3. Parse Results
        // Output format is a 4D Matrix: [1, 1, N, 7]
        // 7 values per detection: [batch_id, class_id, confidence, left, top, right,
        // bottom]
        Mat detections = output.reshape(1, (int) output.total() / 7);

        FloatIndexer indexer = detections.createIndexer();

        for (int i = 0; i < detections.rows(); i++) {
            float confidence = indexer.get(i, 2);

            if (confidence > 0.5) { // Confidence Threshold
                int classId = (int) indexer.get(i, 1);

                // Coordinates are normalized (0.0 to 1.0), scale back to frame size
                int left = (int) (indexer.get(i, 3) * frame.cols());
                int top = (int) (indexer.get(i, 4) * frame.rows());
                int right = (int) (indexer.get(i, 5) * frame.cols());
                int bottom = (int) (indexer.get(i, 6) * frame.rows());

                String label = CLASS_NAMES[classId];
                results.add(new Detection(label, confidence, left, top, right, bottom));
            }
        }

        // Cleanup native memory for per-frame objects
        blob.close();
        output.close();
        detections.close();

        return results;
    }

    private String getResourcePath(String path) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(path);
        if (url == null)
            throw new RuntimeException("Resource not found: " + path);
        return new File(url.getFile()).getAbsolutePath();
    }

    // Simple POJO for results
    public record Detection(String label, float confidence, int x, int y, int x2, int y2) {
    }
}