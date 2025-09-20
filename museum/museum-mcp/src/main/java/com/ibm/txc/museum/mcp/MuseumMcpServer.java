package com.ibm.txc.museum.mcp;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import io.quarkiverse.mcp.server.Tool;
import jakarta.inject.Singleton;

@Singleton
public class MuseumMcpServer {

    @Tool(name = "getTimeContext", description = "Return current time context")
    public TimeContext getTimeContext() {
        var now = OffsetDateTime.now();
        return new TimeContext(
                now.toString(),
                now.getYear(),
                now.format(DateTimeFormatter.ISO_LOCAL_DATE),
                now.getDayOfWeek().toString());
    }

    @Tool(name = "getArtNews", description = "Fetch latest updates from Wikipedia")
    public News getArtNews(String title) {
        if (title == null || title.isBlank())
            return new News("No title provided");
        try {
            HttpClient http = HttpClient.newHttpClient();
            // Use the more reliable MediaWiki API instead of REST API
            String url = "https://en.wikipedia.org/w/api.php?action=query&format=json&prop=extracts&exintro=true&explaintext=true&titles="
                    +
                    java.net.URLEncoder.encode(title, java.nio.charset.StandardCharsets.UTF_8);
            HttpResponse<String> resp = http.send(
                    HttpRequest.newBuilder(URI.create(url))
                            .header("User-Agent", "MuseumMCP/1.0 (Educational Project; markus@jboss.org)")
                            .GET()
                            .build(),
                    HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() == 200) {
                return new News(resp.body());
            } else {
                return new News("Error: HTTP " + resp.statusCode() + " - " + resp.body());
            }
        } catch (Exception e) {
            return new News("Error: " + e.getMessage());
        }
    }

    public record TimeContext(String iso, int year, String today, String weekday) {
    }

    public record News(String latestUpdates) {
    }
}