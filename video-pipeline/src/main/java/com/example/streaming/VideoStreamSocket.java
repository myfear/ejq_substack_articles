package com.example.streaming;

import com.example.service.ImageProcessorService;
import io.quarkus.websockets.next.OnBinaryMessage;
import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;

@WebSocket(path = "/stream/video")
public class VideoStreamSocket {

    @Inject
    ImageProcessorService imageProcessor;

    private final AtomicBoolean streaming = new AtomicBoolean(false);

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        Log.info("Client connected! Ready for bidirectional streaming...");
        streaming.set(true);
    }

    @OnBinaryMessage
    public void onBinaryMessage(WebSocketConnection connection, byte[] frameData) {
        if (!streaming.get()) {
            return;
        }

        try {
            // Decode incoming JPEG frame from webcam
            ByteArrayInputStream bais = new ByteArrayInputStream(frameData);
            BufferedImage inputImage = ImageIO.read(bais);

            if (inputImage == null) {
                Log.error("Failed to decode incoming frame");
                return;
            }

            // Check if still streaming before processing (client might have disconnected)
            if (!streaming.get()) {
                return;
            }

            // Process the frame (overlay logo)
            byte[] processedJpeg = imageProcessor.overlayLogo(inputImage);

            // Check again if still streaming after processing (processing takes time)
            if (!streaming.get()) {
                return;
            }

            // Send processed frame back to client
            // Catch WebSocket closed exception - this is normal when client disconnects
            try {
                connection.sendBinaryAndAwait(processedJpeg);
            } catch (Exception sendException) {
                // If WebSocket is closed, this is expected when client disconnects
                // Don't log as error - it's a normal race condition
                String msg = sendException.getMessage();
                if (msg != null && msg.contains("closed")) {
                    // Client disconnected while processing - this is fine
                    streaming.set(false);
                    return;
                }
                // Re-throw if it's a different error
                throw sendException;
            }

        } catch (Exception e) {
            // Only log if it's not a WebSocket closed exception
            String msg = e.getMessage();
            if (msg == null || !msg.contains("closed")) {
                Log.errorf(e, "Error processing frame: %s", e.getMessage());
            }
        }
    }

    @OnTextMessage
    public void onTextMessage(WebSocketConnection connection, String message) {
        if ("START".equals(message)) {
            streaming.set(true);
            Log.info("Stream started by client");
            connection.sendText("STREAM_STARTED");
        } else if ("STOP".equals(message)) {
            streaming.set(false);
            Log.info("Stream stopped by client");
            connection.sendText("STREAM_STOPPED");
        }
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        streaming.set(false);
        Log.info("Client disconnected");
    }
}