package com.example;

import com.example.service.PolaroidService;
import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@QuarkusTest
public class PolaroidTest {

    @Inject
    PolaroidService polaroidService;

    @Test
    public void generatePolaroidCollage() throws Exception {
        // 1. Collect Input Images
        List<Path> inputs = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            // Ensure you have test1.jpg ... test6.jpg in src/test/resources/
            Path p = Paths.get("src/test/resources/test" + i + ".jpg");
            if (Files.exists(p)) {
                inputs.add(p);
            } else {
                Log.info("Missing: " + p.toAbsolutePath());
            }
        }

        if (inputs.isEmpty()) {
            Log.info("No inputs found. Skipping test.");
            return;
        }

        // 2. Generate
        Log.info("📸 Generating Collage...");
        byte[] result = polaroidService.createCollage(inputs);

        // 3. Save Output
        Path outputDir = Paths.get("target");
        Files.createDirectories(outputDir);
        Path outputFile = outputDir.resolve("polaroid_collage.jpg");

        Files.write(outputFile, result);
        Log.info("Saved to: " + outputFile.toAbsolutePath());
    }
}