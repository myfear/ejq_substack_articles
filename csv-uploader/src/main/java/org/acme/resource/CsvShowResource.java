package org.acme.resource;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.entity.CsvUploadMetadata;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Map;

@Path("/csv-show")
@Produces(MediaType.APPLICATION_JSON)
public class CsvShowResource {

    @Inject
    EntityManager entityManager;

    /**
     * Lists all uploaded dynamic tables, returning their metadata.
     */
    @GET
    @Path("/tables")
    public List<Map<String, Object>> listUploadedTables() {
        return CsvUploadMetadata.<CsvUploadMetadata>listAll().stream()
            .map(metadata -> Map.<String, Object>of(
                "uid", metadata.tableName,
                "uploadTime", metadata.uploadTime,
                "recordCount", metadata.recordCount
            ))
            .toList();
    }

    /**
     * Returns the content of a specific dynamic table as JSON.
     */
    @GET
    @Path("/table/{tableName}")
    @Transactional
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> showTableContent(@PathParam("tableName") String tableName) {
        if (!isTableAllowed(tableName)) {
            throw new IllegalArgumentException("Table '" + tableName + "' not found. This endpoint is for viewing dynamic CSV tables (with UUID names), not metadata tables. Please upload a CSV file first to create a dynamic table.");
        }

        String sql = "SELECT * FROM " + tableName;
        List<Object[]> rows = (List<Object[]>) entityManager.createNativeQuery(sql, Object[].class).getResultList();
        
        // Get column names from database metadata
        List<String> columnNames = entityManager.createNativeQuery(
            "SELECT column_name FROM information_schema.columns WHERE table_name = :tableName ORDER BY ordinal_position",
            String.class)
            .setParameter("tableName", tableName)
            .getResultList();

        if (columnNames.isEmpty()) {
            throw new RuntimeException("Could not find column information for table: " + tableName);
        }

        return mapResults(rows, columnNames);
    }

    private boolean isTableAllowed(String tableName) {
        // Only allow tables created through this system (UUID format)
        return CsvUploadMetadata.find("tableName", tableName).firstResult() != null;
    }

    private List<Map<String, Object>> mapResults(List<Object[]> rows, List<String> columnNames) {
        return rows.stream().map(row -> {
            Map<String, Object> map = new java.util.HashMap<>();
            for (int i = 0; i < row.length; i++) {
                map.put(columnNames.get(i), row[i]);
            }
            return map;
        }).toList();
    }
}
