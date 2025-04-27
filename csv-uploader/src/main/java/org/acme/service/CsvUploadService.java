package org.acme.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.entity.CsvUploadMetadata;
import org.acme.util.CsvReader;
import org.acme.util.TypeInference;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@ApplicationScoped
public class CsvUploadService {

    @Inject
    EntityManager entityManager;

    @Inject
    CsvReader csvReader;

    @Transactional
    public CsvUploadMetadata uploadCsv(FileUpload fileUpload) throws IOException {
        List<Map<String, String>> rows = csvReader.parseCsv(fileUpload);

        if (rows.isEmpty()) {
            throw new RuntimeException("Empty CSV file");
        }

        String tableName = "csv_" + UUID.randomUUID().toString().replace("-", "");

        Map<String, String> sampleRow = rows.get(0);
        Map<String, String> columnTypes = new HashMap<>();
        sampleRow.forEach((key, value) -> {
            if (key == null || key.trim().isEmpty()) {
                throw new RuntimeException("Invalid column name detected in CSV header.");
            }
            columnTypes.put(key, TypeInference.inferType(value));
        });

        String createTableSql = "CREATE TABLE " + tableName + " (id SERIAL PRIMARY KEY, " +
                columnTypes.entrySet().stream()
                        .map(entry -> "\"" + entry.getKey() + "\" " + entry.getValue())
                        .collect(Collectors.joining(", "))
                + ")";
        entityManager.createNativeQuery(createTableSql).executeUpdate();

        for (Map<String, String> row : rows) {
            String columns = row.keySet().stream()
                    .map(col -> "\"" + col + "\"")
                    .collect(Collectors.joining(", "));

            String values = row.values().stream()
                    .map(value -> value == null ? "NULL" : "'" + value.replace("'", "''") + "'")
                    .collect(Collectors.joining(", "));

            String insertSql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + values + ")";
            entityManager.createNativeQuery(insertSql).executeUpdate();
        }

        CsvUploadMetadata metadata = new CsvUploadMetadata();
        metadata.tableName = tableName;
        metadata.uploadTime = LocalDateTime.now();
        metadata.recordCount = rows.size();
        metadata.persist();

        return metadata;
    }
}
