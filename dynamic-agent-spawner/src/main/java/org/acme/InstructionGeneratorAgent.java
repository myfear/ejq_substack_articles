package org.acme;

import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService // No change here
public interface InstructionGeneratorAgent {

    @UserMessage("""
        You are a project manager. A user wants to achieve a goal.
        Your job is to create two distinct sets of instructions for two different AI agents: a PLANNER and a WRITER.

        The PLANNER agent's job is to create a structured outline or a list of key points.
        The WRITER agent's job is to take the planner's output and write the final, polished text.

        Respond with ONLY a valid JSON object that adheres to the following structure, with no preamble:
        {
          "plannerInstructions": "Instructions for the AI planner...",
          "writerInstructions": "Instructions for the AI writer..."
        }

        The user's goal is: {{goal}}
        """)
    // Langchain4j will automatically parse the LLM's JSON output into this object.
    DecomposedInstructions generateInstructions(String goal);
}