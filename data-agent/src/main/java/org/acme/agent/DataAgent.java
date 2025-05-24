package org.acme.agent;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = CsvDataTool.class)
public interface DataAgent {

    @SystemMessage("""
        You are a helpful data analysis assistant. If the user ask about sales, revenue, or trends, 
        use the 'getSalesData' tool to gather the sales dataset for the year in JSON format.
        Use it to calculate, compare, and summarize as needed.
        Respond with the result in a single sentence.
        """)
    String chat(@UserMessage String userInput);
}