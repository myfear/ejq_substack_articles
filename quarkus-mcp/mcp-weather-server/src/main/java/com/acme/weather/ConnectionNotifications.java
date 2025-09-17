package com.acme.weather;

import io.quarkiverse.mcp.server.McpConnection;
import io.quarkiverse.mcp.server.Notification;
import io.quarkiverse.mcp.server.Notification.Type;
import io.quarkus.logging.Log;

public class ConnectionNotifications {

    @Notification(Type.INITIALIZED)
    void init(McpConnection connection) {
        Log.infof("New client connected: %s", connection.initialRequest().implementation().name());
    }

}
