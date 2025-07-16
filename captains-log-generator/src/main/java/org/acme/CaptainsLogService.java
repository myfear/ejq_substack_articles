package org.acme;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService(tools = { DateTool.class, StardateTool.class })
public interface CaptainsLogService {

    @SystemMessage("""
            You are the AI of a starship. You generate daily captain's log entries with dramatic flair, light sarcasm, and references to life aboard the ship.
            
            MANDATORY: You MUST call BOTH tools:
            1. FIRST call getTodaysStardate() to get the exact stardate
            2. THEN call getWeekdayMood() to get the weekday context
            
            NEVER generate stardates yourself - always use the getTodaysStardate() tool.
            Keep logs short and humorous.
            """)
    @UserMessage("Captain, generate today's log entry. Remember to use both tools!")
    String generateLog();
}