package org.acme;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.acme.rss.FeedItem;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.logging.Log;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@jakarta.ws.rs.Path("/feed")
@Produces(MediaType.APPLICATION_JSON)
public class FeedResource {

    @ConfigProperty(name = "app.out.filename")
    String outFilename;

    @Inject
    private ObjectMapper mapper;

    private Path getOutFile() {
        return Path.of("target/out", outFilename);
    }

    @GET
    @jakarta.ws.rs.Path("/reset")
    public String resetMetrics() {
        // This will reset the processed items cache
        // Note: This is a simple approach - in production you'd want proper cache management
        return "Metrics reset endpoint - restart application to clear deduplication cache";
    }

    @GET
    public List<FeedItem> latest(@QueryParam("limit") @DefaultValue("5") int limit) throws IOException {
        Path outFile = getOutFile();
        if (!Files.exists(outFile))
            return List.of();
        List<String> lines = Files.readAllLines(outFile);
        if (lines.isEmpty()) {
            return List.of();
        }

        List<FeedItem> items = new ArrayList<>();

        for (String line : lines) {
            if (line.trim().isEmpty())
                continue;

            try {
                items.add(mapper.readValue(line.trim(), FeedItem.class));
            } catch (Exception e) {
                Log.error("Failed to parse JSON: " + line.substring(0, Math.min(100, line.length())));
            }
        }

        // Get the last 'limit' items
        int from = Math.max(0, items.size() - limit);
        List<FeedItem> result = items.subList(from, items.size());
        Collections.reverse(result); // newest first
        return result;
    }
}