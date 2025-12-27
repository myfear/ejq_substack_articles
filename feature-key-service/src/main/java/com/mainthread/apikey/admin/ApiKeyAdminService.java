package com.mainthread.apikey.admin;

import java.time.Instant;
import java.util.Set;

import com.mainthread.apikey.admin.dto.CreateKeyRequest;
import com.mainthread.apikey.admin.dto.CreateKeyResponse;
import com.mainthread.apikey.admin.dto.RotateKeyResponse;
import com.mainthread.apikey.keys.ApiKeyCrypto;
import com.mainthread.apikey.keys.ApiKeyEntity;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ApiKeyAdminService {

    private final ApiKeyCrypto crypto;

    public ApiKeyAdminService(ApiKeyCrypto crypto) {
        this.crypto = crypto;
    }

    @Transactional
    public CreateKeyResponse create(CreateKeyRequest request) {
        ApiKeyCrypto.GeneratedKey key = crypto.generate();

        ApiKeyEntity entity = new ApiKeyEntity();
        entity.keyId = key.keyId();
        entity.keyHash = key.secretHash();
        entity.active = true;
        entity.createdAt = Instant.now();
        entity.lastUsedAt = Instant.EPOCH;
        entity.features.addAll(request.features());

        entity.persist();

        return new CreateKeyResponse(
                entity.keyId,
                key.presentedKey(),
                Set.copyOf(entity.features),
                entity.createdAt);
    }

    @Transactional
    public void revoke(String keyId) {
        ApiKeyEntity entity = ApiKeyEntity.find("keyId", keyId).firstResult();
        if (entity == null) {
            return;
        }
        entity.active = false;
    }

    @Transactional
    public RotateKeyResponse rotate(String keyId) {
        ApiKeyEntity entity = ApiKeyEntity.find("keyId", keyId).firstResult();
        if (entity == null) {
            throw new IllegalArgumentException("Unknown keyId: " + keyId);
        }

        ApiKeyCrypto.GeneratedKey newKey = crypto.generate();
        entity.keyHash = newKey.secretHash();
        entity.active = true;

        return new RotateKeyResponse(entity.keyId, newKey.presentedKey(), Instant.now());
    }
}
