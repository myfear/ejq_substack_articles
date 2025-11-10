package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Path("/deduplicate")
@Produces(MediaType.APPLICATION_JSON)
public class DeduplicationResource {

    @Inject
    DeduplicationService deduplicationService;

    /**
     * Find duplicate files in a directory by comparing their SHA-256 hashes.
     * 
     * Example usage:
     * GET /deduplicate?path=/tmp/test-files
     * 
     * Returns a JSON object mapping hash values to lists of file paths that have
     * that hash.
     * Only includes hashes that appear in 2 or more files (actual duplicates).
     * 
     * @param path The directory path to scan for duplicates
     * @return JSON response with duplicate file information
     */
    @GET
    public Response findDuplicates(@QueryParam("path") String path) {
        if (path == null || path.trim().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Path parameter is required"))
                    .build();
        }

        File directory = new File(path);

        if (!directory.exists()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("error", "Directory does not exist: " + path))
                    .build();
        }

        if (!directory.isDirectory()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Path is not a directory: " + path))
                    .build();
        }

        try {
            Map<String, List<String>> duplicates = deduplicationService.findDuplicates(directory);

            if (duplicates.isEmpty()) {
                return Response.ok(Map.of(
                        "message", "No duplicate files found",
                        "scannedPath", path,
                        "duplicates", Map.<String, List<String>>of())).build();
            }

            // Calculate summary statistics
            int totalDuplicateGroups = duplicates.size();
            int totalDuplicateFiles = duplicates.values().stream()
                    .mapToInt(List::size)
                    .sum();
            int totalWastedFiles = totalDuplicateFiles - totalDuplicateGroups; // files that could be removed

            return Response.ok(Map.of(
                    "scannedPath", path,
                    "summary", Map.of(
                            "duplicateGroups", totalDuplicateGroups,
                            "totalDuplicateFiles", totalDuplicateFiles,
                            "filesThatCanBeRemoved", totalWastedFiles),
                    "duplicates", duplicates)).build();

        } catch (IOException e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "Error scanning directory: " + e.getMessage()))
                    .build();
        } catch (SecurityException e) {
            return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error", "Access denied: " + e.getMessage()))
                    .build();
        }
    }
}
