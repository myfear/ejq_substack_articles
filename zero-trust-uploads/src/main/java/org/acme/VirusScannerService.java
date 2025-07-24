package org.acme;

import java.io.InputStream;
import java.util.List;

import io.quarkiverse.antivirus.runtime.Antivirus;
import io.quarkiverse.antivirus.runtime.AntivirusScanResult;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class VirusScannerService {

    @Inject
    Antivirus antivirus;

    public Uni<List<AntivirusScanResult>> scanFileReactive(String fileName, InputStream inputStream) {
        System.out.println("Starting reactive virus scan for file: " + fileName);
        
        // Wrap the blocking antivirus scan in a reactive context
        // This moves the blocking operation to a worker thread
        return Uni.createFrom().item(() -> {
            System.out.println("Scanning file on worker thread: " + fileName);
            return antivirus.scan(fileName, inputStream);
        }).runSubscriptionOn(io.smallrye.mutiny.infrastructure.Infrastructure.getDefaultWorkerPool());
    }
}