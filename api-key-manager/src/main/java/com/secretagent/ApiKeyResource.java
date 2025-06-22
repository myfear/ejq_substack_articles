package com.secretagent;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api/keys")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "API Key Management", description = "Secret Agent API Key Operations")
public class ApiKeyResource {

    @Inject
    ApiKeyService apiKeyService;


    @POST
    @Operation(summary = "Create a new API key", description = "Generate a new API key for an agent")
    public Response createApiKey(CreateApiKeyRequest request) {
        try {
            ApiKey apiKey = apiKeyService.createApiKey(
                    request.name,
                    request.owner,
                    request.validityDays,
                    request.permissions);
            return Response.status(Response.Status.CREATED)
                    .entity(new ApiKeyResponse(apiKey))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(new ErrorResponse("Failed to create API key: " + e.getMessage()))
                    .build();
        }
    }

    @GET
    @Path("/owner/{owner}")
    @Operation(summary = "Get keys by owner", description = "Retrieve all keys for a specific agent")
    public List<ApiKeyResponse> getKeysByOwner(@PathParam("owner") String owner) {
        return apiKeyService.getKeysByOwner(owner)
                .stream()
                .map(ApiKeyResponse::new)
                .toList();
    }

    @POST
    @Path("/{keyId}/rotate")
    @Operation(summary = "Rotate an API key", description = "Create a new key and deactivate the old one")
    public Response rotateKey(@PathParam("keyId") Long keyId) {
        Optional<ApiKey> newKey = apiKeyService.rotateKey(keyId);
        if (newKey.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(new ErrorResponse("API key not found"))
                    .build();
        }
        return Response.ok(new ApiKeyResponse(newKey.get())).build();
    }

    @POST
    @Path("/validate")
    @Operation(summary = "Validate API key", description = "Check if key is valid and record usage")
    public Response validateKey(ValidateKeyRequest request) {
        boolean isValid = apiKeyService.validateAndRecordUsage(request.keyValue);
        return Response.ok(new ValidationResponse(isValid)).build();
    }

    @GET
    @Path("/stats")
    @Operation(summary = "Get API key statistics", description = "Get usage statistics for all keys")
    public Response getStats() {
        List<ApiKey> activeKeys = apiKeyService.getAllActiveKeys();
        long totalUsage = activeKeys.stream().mapToLong(key -> key.usageCount).sum();

        return Response.ok(new StatsResponse(
                activeKeys.size(),
                totalUsage,
                activeKeys.stream().mapToLong(key -> key.usageCount).max().orElse(0))).build();
    }

    // DTOs for our secret communications
    public static class CreateApiKeyRequest {
        public String name;
        public String owner;
        public Integer validityDays;
        public String permissions;
    }

    public static class ValidateKeyRequest {
        public String keyValue;
    }

    public static class ApiKeyResponse {
        public Long id;
        public String keyValue;
        public String name;
        public String owner;
        public String createdAt;
        public String lastUsed;
        public Boolean active;
        public Long usageCount;
        public String expiresAt;

        public ApiKeyResponse(ApiKey apiKey) {
            this.id = apiKey.id;
            this.keyValue = apiKey.keyValue;
            this.name = apiKey.name;
            this.owner = apiKey.owner;
            this.createdAt = apiKey.createdAt.toString();
            this.lastUsed = apiKey.lastUsed != null ? apiKey.lastUsed.toString() : null;
            this.active = apiKey.active;
            this.usageCount = apiKey.usageCount;
            this.expiresAt = apiKey.expiresAt != null ? apiKey.expiresAt.toString() : null;
        }
    }

    public static class ValidationResponse {
        public boolean valid;
        public String message;

        public ValidationResponse(boolean valid) {
            this.valid = valid;
            this.message = valid ? "Access granted, Agent!" : "Access denied. Invalid credentials.";
        }
    }

    public static class StatsResponse {
        public int totalActiveKeys;
        public long totalUsage;
        public long maxUsage;

        public StatsResponse(int totalActiveKeys, long totalUsage, long maxUsage) {
            this.totalActiveKeys = totalActiveKeys;
            this.totalUsage = totalUsage;
            this.maxUsage = maxUsage;
        }
    }

    public static class ErrorResponse {
        public String error;

        public ErrorResponse(String error) {
            this.error = error;
        }
    }
}