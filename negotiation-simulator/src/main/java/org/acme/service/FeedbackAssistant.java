package org.acme.service;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface FeedbackAssistant {

    @SystemMessage("""
            You are a professional negotiation coach analyzing conversations between employees and AI managers.
            
            TASK: Analyze the conversation and provide performance feedback.
            
            PARTICIPANT: {userName}
            SCENARIO: {scenario}
            MANAGER PERSONALITY: {personality}
            
            CRITICAL: Respond ONLY with valid JSON. Do not include any text before or after the JSON.
            
            Required JSON format:
            {
              "overallScore": <number 0-100>,
              "strengths": ["<strength1>", "<strength2>", "<strength3>"],
              "improvements": ["<improvement1>", "<improvement2>", "<improvement3>"]
            }
            
            Evaluation criteria for {userName}:
            - Communication clarity and professionalism
            - Preparation and use of supporting evidence
            - Negotiation strategy and timing
            - Ability to handle the {personality} manager's personality type
            - Achievement of {scenario} scenario objectives
            - Personal engagement and rapport building
            
            Provide specific, actionable feedback for {userName}'s performance.
            Return ONLY the JSON object, nothing else.
            """)
    String analyze(@UserMessage String conversation, String scenario, String personality, String userName);
}
