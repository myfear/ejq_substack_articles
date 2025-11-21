package com.example;

import io.quarkus.websockets.next.OnBinaryMessage;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.inject.Inject;

import java.util.logging.Logger;

@WebSocket(path = "/stream")
public class VideoSocket {

    private static final Logger LOG = Logger.getLogger(VideoSocket.class.getName());

    @Inject
    FaceDetectorService faceDetector;

    @Inject
    SentimentService sentiment;

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        LOG.info("WebSocket connection opened: " + connection.id());
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        LOG.info("WebSocket connection closed: " + connection.id());
    }

    @OnBinaryMessage
    public void onFrame(WebSocketConnection connection, byte[] frameData) {
        LOG.info("Received WebSocket frame: " + frameData.length + " bytes");
        
        try {
            FaceDetectionResult result = faceDetector.detectAndAnnotateFace(frameData);
            LOG.info("Face detection result: " + (result != null ? "face found" : "no face detected"));

            if (result != null) {
                // Send annotated image with bounding box
                byte[] annotatedImage = result.getAnnotatedImage();
                LOG.info("Sending annotated image: " + annotatedImage.length + " bytes");
                connection.sendBinaryAndAwait(annotatedImage);
                
                // Start sentiment analysis on extracted face
                byte[] face = result.getFaceImage();
                LOG.info("Starting sentiment analysis on face: " + face.length + " bytes");
                sentiment.analyzeAsync(face)
                        .subscribe().with(
                            emotion -> {
                                LOG.info("Sentiment analysis result: " + emotion);
                                // Send emotion without confidence (confidence is shown on image overlay)
                                connection.sendTextAndAwait(emotion);
                                LOG.info("Sent emotion to client: " + emotion);
                            },
                            error -> {
                                LOG.severe("Error in sentiment analysis: " + error.getMessage());
                                error.printStackTrace();
                                connection.sendTextAndAwait("Error");
                            }
                        );
            } else {
                LOG.info("No face detected, sending original image and 'No Face' response");
                // Send original image back
                connection.sendBinaryAndAwait(frameData);
                connection.sendTextAndAwait("No Face");
            }
        } catch (Exception e) {
            LOG.severe("Error processing frame: " + e.getMessage());
            e.printStackTrace();
            try {
                // Send original image back and error message
                connection.sendBinaryAndAwait(frameData);
                connection.sendTextAndAwait("Error");
            } catch (Exception ex) {
                LOG.severe("Failed to send error message: " + ex.getMessage());
            }
        }
    }
}
