package org.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class DataIngestor {

    @Inject
    CardinalityService cardinalityService;

    @Inject
    ObjectMapper mapper;

    public long processGhArchiveFile(Path filePath) throws Exception {
        long linesProcessed = 0;
        try (
                FileInputStream fileInputStream = new FileInputStream(filePath.toFile());
                GZIPInputStream gzipInputStream = new GZIPInputStream(fileInputStream);
                InputStreamReader inputStreamReader = new InputStreamReader(gzipInputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                JsonNode event = mapper.readTree(line);

                JsonNode actor = event.get("actor");
                if (actor != null && actor.has("login")) {
                    cardinalityService.trackUser(actor.get("login").asText());
                }

                JsonNode repo = event.get("repo");
                if (repo != null && repo.has("name")) {
                    cardinalityService.trackRepo(repo.get("name").asText());
                }
                linesProcessed++;
            }
        }
        return linesProcessed;
    }
}