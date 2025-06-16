package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

public interface TaskExecutionAgent {

    @SystemMessage("""
            You are a specialized agent, an expert in following instructions.
            Your instructions are as follows:
            ---
            {{instructions}}
            ---
            Execute the task based ONLY on these instructions.
            """)
    String executeTask(@UserMessage String details, String instructions);
}