package org.crdt.ai.websocket;

import java.util.UUID;

import org.crdt.ai.AiAssistant;
import org.crdt.ai.CrdtCharacter;
import org.crdt.ai.DocumentState;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import io.quarkus.logging.Log;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@WebSocket(path = "/editor")
@ApplicationScoped
public class DocumentSocket {

    @Inject
    DocumentState documentState;

    @Inject
    AiAssistant assistant;

    @Inject
    ObjectMapper objectMapper; // For JSON conversion

    @OnOpen
    public void onOpen(WebSocketConnection connection) {
        // When a new user connects, send them the current document state
        // so they are in sync.
        try {
            WsMessage syncMessage = new WsMessage("SYNC_DOCUMENT",
                    objectMapper.valueToTree(documentState.getFullDocument()));
            connection.sendText(objectMapper.writeValueAsString(syncMessage));
        } catch (JsonProcessingException e) {
            // Log error
        }
    }

    @OnTextMessage(broadcast = true) // Broadcast messages to all connected clients
    public String onMessage(WebSocketConnection connection, String message) {
        try {
            WsMessage wsMessage = objectMapper.readValue(message, WsMessage.class);
            JsonNode payload = wsMessage.payload();

            switch (wsMessage.type()) {
                case "INSERT":
                    CrdtCharacter character = objectMapper.treeToValue(payload.get("char"), CrdtCharacter.class);
                    int index = payload.get("index").asInt();
                    documentState.insert(index, character);
                    // This message will be broadcasted to all clients, including the sender
                    return message;

                case "DELETE":
                    UUID charId = UUID.fromString(payload.get("charId").asText());
                    documentState.delete(charId);
                    // Broadcast the deletion instruction
                    return message;

                case "GET_SUGGESTION":
                    // AI suggestions should not be broadcasted.
                    // We handle this separately and send it only to the requester.
                    handleSuggestionRequest(connection);
                    return null; // Returning null prevents broadcasting
            }
        } catch (JsonProcessingException e) {
            // Log error
        }
        return null; // Do not broadcast malformed messages
    }

    private void handleSuggestionRequest(WebSocketConnection connection) {
        String currentText = documentState.getTextContent();
        String suggestion;

        Log.infof("Current text: '%s' (length: %d, isBlank: %s)",
                currentText, currentText.length(), currentText.isBlank());

        if (currentText.isBlank()) {
            suggestion = "Start by typing a few words, and I'll help you continue!";
        } else {
            suggestion = assistant.suggest(currentText);
        }

        Log.infof("Sending suggestion: %s", suggestion);

        try {
            // Create a payload for the suggestion message
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("suggestion", suggestion);
            WsMessage suggestionMessage = new WsMessage("SUGGESTION", payload);

            // Send the suggestion back to only the requesting client
            String messageJson = objectMapper.writeValueAsString(suggestionMessage);
            Log.infof("Sending message: %s", messageJson);
            connection.sendText(messageJson).subscribe().asCompletionStage();
        } catch (JsonProcessingException e) {
            Log.errorf(e, "Error sending suggestion: %s", e.getMessage());
        }
    }
}
