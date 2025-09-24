package com.ibm.txc.museum.ai;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;
import io.quarkiverse.langchain4j.mcp.runtime.McpToolBox;

@RegisterAiService
public interface ArtInspectorAi {

        @SystemMessage("""
                        Identify artwork from a visitor photo.
                        Return JSON with: title, artist, confidence, why, lates news, today.
                        """)
        @UserMessage("""
                        Photo: {{image}}
                        Use MCP tool getTimeContext to put into a time context.
                        Use MCP tool getArtNews Latest updates about the artwork.
                        """)
        @McpToolBox("museum")
        String identify(Image image);
}