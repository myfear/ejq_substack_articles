package com.acme.client;

import dev.langchain4j.mcp.client.logging.McpLogMessage;
import io.quarkiverse.langchain4j.mcp.runtime.McpClientName;
import jakarta.enterprise.event.Observes;
import io.quarkus.logging.Log;

public class McpLogObserver {

    void onWeatherLogs(@Observes @McpClientName("weather") McpLogMessage msg) {
        Log.info("[MCP-LOG] " + msg.level() + " :: " + msg.toString());
    }
}