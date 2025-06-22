package com.secretagent;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ApiKeyService {

    private static final String KEY_PREFIX = "ak_";
    private static final int KEY_LENGTH = 32;
    private final SecureRandom secureRandom = new SecureRandom();

    @Transactional
    public ApiKey createApiKey(String name, String owner, Integer validityDays, String permissions) {
        ApiKey apiKey = new ApiKey();
        apiKey.keyValue = generateSecureKey();
        apiKey.name = name;
        apiKey.owner = owner;
        apiKey.createdAt = LocalDateTime.now();
        apiKey.permissions = permissions;

        if (validityDays != null && validityDays > 0) {
            apiKey.expiresAt = LocalDateTime.now().plusDays(validityDays);
        }

        apiKey.persist();
        return apiKey;
    }

    private String generateSecureKey() {
        byte[] randomBytes = new byte[KEY_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return KEY_PREFIX + Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    @Transactional
    public Optional<ApiKey> rotateKey(Long keyId) {
        Optional<ApiKey> existingKey = ApiKey.findByIdOptional(keyId);
        if (existingKey.isEmpty()) {
            return Optional.empty();
        }

        ApiKey oldKey = existingKey.get();

        // Create new key with same properties
        ApiKey newKey = createApiKey(
                oldKey.name + " (rotated)",
                oldKey.owner,
                oldKey.expiresAt != null
                        ? (int) java.time.Duration.between(LocalDateTime.now(), oldKey.expiresAt).toDays()
                        : null,
                oldKey.permissions);

        // Deactivate old key
        oldKey.active = false;
        oldKey.persist();

        return Optional.of(newKey);
    }

    @Transactional
    public boolean validateAndRecordUsage(String keyValue) {
        Optional<ApiKey> apiKey = ApiKey.findByKeyValue(keyValue);
        if (apiKey.isEmpty() || !apiKey.get().isValid()) {
            return false;
        }

        apiKey.get().recordUsage();
        return true;
    }

    @Transactional
    public void deactivateExpiredKeys() {
        List<ApiKey> expiredKeys = ApiKey.findExpiredKeys();
        expiredKeys.forEach(key -> {
            key.active = false;
            key.persist();
        });
    }

    public List<ApiKey> getKeysByOwner(String owner) {
        return ApiKey.findByOwner(owner);
    }

    public List<ApiKey> getAllActiveKeys() {
        return ApiKey.findActiveKeys();
    }
}