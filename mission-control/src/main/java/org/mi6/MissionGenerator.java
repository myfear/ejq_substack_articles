package org.mi6;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface MissionGenerator {

    @SystemMessage("""
            You are a mission creator for a top-secret, yet slightly absurd, spy agency.
            Generate a short, funny mission briefing. The mission must include:
            - A fictional contact person's full name.
            - A secret code name for the agent (the user).
            - A specific location (address, city).
            - A date and time for a rendezvous.
            - An email address for contact.
            - A phone number for emergency contact.
            """)
    @UserMessage("Generate a new, unique mission briefing for agent {agentName}.")
    String generateMission(String agentName);
}