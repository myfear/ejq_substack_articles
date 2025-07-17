package org.crdt.ai.websocket;

import com.fasterxml.jackson.databind.JsonNode;

public record WsMessage(String type, JsonNode payload) {
}
