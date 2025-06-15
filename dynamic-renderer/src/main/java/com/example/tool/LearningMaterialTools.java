package com.example.tool;

import java.io.IOException;

import org.jboss.logging.Logger;
import org.jsoup.Jsoup;

import dev.langchain4j.agent.tool.Tool;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class LearningMaterialTools {

    private static final Logger LOG = Logger.getLogger(LearningMaterialTools.class);

    @Tool("Perform a web search to retrieve information online")
    String webSearch(String q) throws IOException {
        String webUrl = "https://html.duckduckgo.com/html/?q=" + q;
        String text = Jsoup.connect(webUrl).get().getElementsByClass("results").text().substring(0, 2000);
        LOG.infof("Parsed search response: %s", text);
        return text;
    }
}