package com.example;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.minio.MinioClient;
import io.minio.RemoveObjectArgs;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class FileStorageService {

    @Inject
    MinioClient minio;

    @ConfigProperty(name = "minio.bucket")
    String bucket;


    private final Map<String, FileMetadata> store = new ConcurrentHashMap<>();

    void put(String id, FileMetadata meta) {
        store.put(id, meta);
    }

    Optional<FileMetadata> get(String id) {
        return Optional.ofNullable(store.get(id));
    }

    void delete(String id) {
        get(id).ifPresent(meta -> {
            try {
                minio.removeObject(
                        RemoveObjectArgs.builder()
                                .bucket(bucket)
                                .object(meta.objectName())
                                .build());
            } catch (Exception e) {
                // log and swallow; cleanup best-effort
            }
            store.remove(id);
        });
    }

}