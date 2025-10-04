package com.example.api;

import com.example.ai.AiPipeline;
import com.example.progress.ProgressEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

@Path("/ai")
public class AskResource {

    @Inject
    AiPipeline pipeline;
    @Inject
    Sse sse;
    @Inject
    ObjectMapper objectMapper;

    @GET
    @Path("/ask/stream")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void ask(@QueryParam("q") String question, SseEventSink eventSink) {
        try {
            pipeline.processWithProgress(question, event -> sendEvent(event, eventSink));
            eventSink.close();
        } catch (Exception e) {
            sendErrorEvent(e.getMessage(), eventSink);
            eventSink.close();
        }
    }

    private void sendEvent(ProgressEvent event, SseEventSink eventSink) {
        try {
            String jsonData = objectMapper.writeValueAsString(event);
            eventSink.send(sse.newEventBuilder()
                    .data(jsonData)
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build());
        } catch (Exception e) {
            // Fallback to manual JSON construction
            sendEventManually(event, eventSink);
        }
    }

    private void sendErrorEvent(String errorMessage, SseEventSink eventSink) {
        ProgressEvent errorEvent = new ProgressEvent("Error", 0, errorMessage);
        sendEvent(errorEvent, eventSink);
    }

    private void sendEventManually(ProgressEvent event, SseEventSink eventSink) {
        try {
            String jsonData = String.format("{\"step\":\"%s\",\"percent\":%d,\"detail\":\"%s\"}", 
                    event.step, event.percent, event.detail);
            eventSink.send(sse.newEventBuilder()
                    .data(jsonData)
                    .mediaType(MediaType.APPLICATION_JSON_TYPE)
                    .build());
        } catch (Exception e) {
            // If even manual construction fails, close the sink
            eventSink.close();
        }
    }
}