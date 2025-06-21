package com.example.service;

import com.example.ai.FlightDataTools;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@RegisterAiService(tools = FlightDataTools.class)
public interface AviationAiService {

    @SystemMessage("""
            You are an intelligent aviation assistant with access to real-time flight data.
            Your primary function is to use the available tools to answer user questions about aviation.
            - Always use tools when users ask about specific flights or locations.
            - Provide context and explanations, not just raw data. Be helpful and educational.
            - Alert users if you find emergency or unusual situations.
            - Use nautical miles for distances.

            Major airports coordinates for reference:
            - Frankfurt (FRA): 50.0379, 8.5622
            - Munich (MUC): 48.3537, 11.7863
            - Berlin (BER): 52.3667, 13.5033
            - London Heathrow (LHR): 51.4700, -0.4543
            - Paris CDG (CDG): 49.0097, 2.5479
            """)
    String processQuery(@UserMessage String userMessage);

}