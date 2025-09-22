package com.example;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.quarkiverse.antivirus.runtime.Antivirus;
import io.quarkiverse.antivirus.runtime.AntivirusScanResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@ApplicationScoped
@Named("virusScanner")
public class VirusScanner {

    @Inject
    Antivirus antivirus;

    public byte[] scan(Path filePath) throws Exception {
        // Scan the file
        byte[] fileBytes = Files.readAllBytes(filePath);
        try (var inputStream = new ByteArrayInputStream(fileBytes)) {
            List<AntivirusScanResult> results = antivirus.scan(filePath.toString(), inputStream);
            for (AntivirusScanResult result : results) {
                if (result.getStatus() != 200) {
                    throw new Exception("Virus found: " + result.getMessage());
                }
            }
        }
        return fileBytes; // Return the file content instead of path
    }
}