package com.example.vision;

import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicLong;

@Path("/stream")
public class StreamResource {

    @Inject
    WebcamService webcamService;

    @GET
    @Produces("multipart/x-mixed-replace;boundary=frame")
    public Multi<byte[]> stream() {
        final byte[] initialBoundary = "--frame\r\n".getBytes(StandardCharsets.UTF_8);
        final byte[] frameBoundary = "\r\n--frame\r\n".getBytes(StandardCharsets.UTF_8);
        final AtomicLong frameIndex = new AtomicLong(0);
        
        return webcamService.stream()
                .map(bytes -> {
                    long index = frameIndex.getAndIncrement();
                    
                    // Use initial boundary for first frame, frame boundary for subsequent frames
                    byte[] boundaryToUse = (index == 0) ? initialBoundary : frameBoundary;
                    
                    // Format each MJPEG frame with boundary and headers
                    String headers = String.format(
                        "Content-Type: image/jpeg\r\nContent-Length: %d\r\n\r\n",
                        bytes.length
                    );
                    
                    byte[] headerBytes = headers.getBytes(StandardCharsets.UTF_8);
                    byte[] frame = new byte[boundaryToUse.length + headerBytes.length + bytes.length];
                    
                    int offset = 0;
                    System.arraycopy(boundaryToUse, 0, frame, offset, boundaryToUse.length);
                    offset += boundaryToUse.length;
                    System.arraycopy(headerBytes, 0, frame, offset, headerBytes.length);
                    offset += headerBytes.length;
                    System.arraycopy(bytes, 0, frame, offset, bytes.length);
                    
                    return frame;
                });
    }
}