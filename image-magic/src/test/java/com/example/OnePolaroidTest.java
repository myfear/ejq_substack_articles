package com.example;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import com.example.service.PolaroidService;

import io.quarkus.logging.Log;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
public class OnePolaroidTest {

    @Inject
    PolaroidService onePolaroidService;

    @Test
    public void generateOnePolaroid() throws Exception {
        // 1. Get input image
        Path inputImage = Paths.get("src/test/resources/test1.jpg");

        if (!Files.exists(inputImage)) {
            Log.info("⚠️ Missing: " + inputImage.toAbsolutePath());
            Log.info("Skipping test.");
            return;
        }

        // 2. Generate standard polaroid effect
        Log.info("📸 Generating Polaroid Effect...");
        byte[] result = onePolaroidService.createPolaroidFromBytes(Files.readAllBytes(inputImage));

        // 3. Save Output
        Path outputDir = Paths.get("target");
        Files.createDirectories(outputDir);
        Path outputFile = outputDir.resolve("one_polaroid.png");

        Files.write(outputFile, result);
        Log.info("✅ Saved to: " + outputFile.toAbsolutePath());
        Log.info("✅ Output size: " + result.length + " bytes");
    }
}
