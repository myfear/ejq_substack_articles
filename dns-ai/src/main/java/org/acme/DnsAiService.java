package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.ToolBox;

@RegisterAiService
public interface DnsAiService {

    @SystemMessage("You are a DNS expert. Use the available tools to find A, MX, and CNAME records for the given domain and provide a summary to the user.")
    @ToolBox(DnsTools.class)
    String getDnsInfo(@UserMessage String domain);
}