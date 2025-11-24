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

/**
 * WebSocket endpoint for bidirectional video streaming.
 * <p>
 * This class provides a WebSocket endpoint at {@code /stream/video} that enables
 * clients to send video frames (as JPEG images) and receive processed frames back.
 * Each incoming frame is processed by the {@link ImageProcessorService} which applies
 * denoising filters and overlays a logo before sending the processed frame back to the client.
 * </p>
 * <p>
 * The service supports control messages:
 * <ul>
 *   <li>{@code START} - Starts the video streaming</li>
 *   <li>{@code STOP} - Stops the video streaming</li>
 * </ul>
 * </p>
 * <p>
 * The streaming state is managed atomically to handle client disconnections gracefully.
 * </p>
 *
 * @author Generated
 * @see ImageProcessorService
 */
@WebSocket(path = "/stream/video")
public class VideoStreamSocket {

    /**
     * Service for processing video frames (denoising and logo overlay).
     */
    @Inject
    ImageProcessorService imageProcessor;

    /**
     * Atomic flag to track whether video streaming is currently active.
     * Used to prevent processing frames after a client disconnects.
     */
    private final AtomicBoolean streaming = new AtomicBoolean(false);

    /**
     * Called when a WebSocket client connects to the endpoint.
     * <p>
     * Initializes the streaming state and logs the connection.
     * </p>
     *
     * @param connection the WebSocket connection that was established
     */
    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        Log.info("Client connected! Ready for bidirectional streaming...");
        streaming.set(true);
    }

    /**
     * Handles incoming binary messages containing JPEG video frames.
     * <p>
     * This method processes each frame as follows:
     * <ol>
     *   <li>Decodes the incoming JPEG bytes into a {@link BufferedImage}</li>
     *   <li>Checks if streaming is still active (client may have disconnected)</li>
     *   <li>Processes the frame using {@link ImageProcessorService#overlayLogo(BufferedImage)}
     *       which applies denoising and overlays a logo</li>
     *   <li>Sends the processed JPEG frame back to the client</li>
     * </ol>
     * </p>
     * <p>
     * The method includes multiple checks for streaming state to handle client disconnections
     * gracefully. WebSocket closed exceptions are handled silently as they are expected
     * when clients disconnect during frame processing.
     * </p>
     *
     * @param connection the WebSocket connection
     * @param frameData  the JPEG frame data as a byte array
     */
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

    /**
     * Handles incoming text control messages from the client.
     * <p>
     * Supported commands:
     * <ul>
     *   <li>{@code START} - Enables video streaming and responds with {@code STREAM_STARTED}</li>
     *   <li>{@code STOP} - Disables video streaming and responds with {@code STREAM_STOPPED}</li>
     * </ul>
     * </p>
     *
     * @param connection the WebSocket connection
     * @param message    the control message from the client
     */
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

    /**
     * Called when a WebSocket client disconnects from the endpoint.
     * <p>
     * Cleans up the streaming state by setting the streaming flag to false.
     * This prevents any pending frame processing from continuing after disconnection.
     * </p>
     *
     * @param connection the WebSocket connection that was closed
     */
    @OnClose
    public void onClose(WebSocketConnection connection) {
        streaming.set(false);
        Log.info("Client disconnected");
    }
}