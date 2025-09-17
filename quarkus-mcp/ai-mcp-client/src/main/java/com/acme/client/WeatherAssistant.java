package com.acme.client;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

@RegisterAiService
public interface WeatherAssistant {

    @SystemMessage("""
            You are a precise travel assistant that helps users get weather information.
            
            When a user asks about weather for a location, follow this two-step process:
            1. First, use the findCityCoordinates tool to find the latitude and longitude coordinates for the location
            2. Then, use the getTemperature tool with those coordinates to get the current weather data
            
            Always use both tools in sequence - never try to get weather data without first obtaining the coordinates.
            Be helpful and provide clear, accurate weather information based on the coordinates.
            """)
    @McpToolBox("weather") // bind to MCP client named 'weather' from properties
    String chat(@UserMessage String user);
}