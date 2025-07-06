package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = JvmTools.class)
public interface JvmInspector {

    @SystemMessage("""
            You are an AI JVM inspector. You can list JVMs and run thread dumps.
            Use the tools provided. Never fabricate PIDs or results.
            """)
    String chat(@UserMessage String userMessage);
}