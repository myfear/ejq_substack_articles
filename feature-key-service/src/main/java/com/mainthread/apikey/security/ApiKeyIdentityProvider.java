package com.mainthread.apikey.security;

import java.security.Principal;
import java.time.Instant;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import com.mainthread.apikey.keys.ApiKeyCrypto;
import com.mainthread.apikey.keys.ApiKeyEntity;

import io.quarkus.security.identity.AuthenticationRequestContext;
import io.quarkus.security.identity.IdentityProvider;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ApiKeyIdentityProvider implements IdentityProvider<ApiKeyAuthenticationRequest> {

    private final ApiKeyCrypto crypto;

    @ConfigProperty(name = "app.apikey.bootstrap-admin")
    String bootstrapAdminKey;

    public ApiKeyIdentityProvider(ApiKeyCrypto crypto) {
        this.crypto = crypto;
    }

    @Override
    public Class<ApiKeyAuthenticationRequest> getRequestType() {
        return ApiKeyAuthenticationRequest.class;
    }

    @Override
    public Uni<SecurityIdentity> authenticate(ApiKeyAuthenticationRequest request,
            AuthenticationRequestContext context) {
        return context.runBlocking(() -> authenticateBlocking(request.rawApiKey()));
    }

    @Transactional
    SecurityIdentity authenticateBlocking(String rawApiKey) {
        // Bootstrap admin: lets you manage keys before you have keys.
        if (rawApiKey.equals(bootstrapAdminKey)) {
            return QuarkusSecurityIdentity.builder()
                    .setPrincipal(simplePrincipal("bootstrap-admin"))
                    .addRole("admin")
                    .build();
        }

        ParsedKey parsed = ParsedKey.parse(rawApiKey);
        if (parsed == null) {
            return null;
        }

        ApiKeyEntity entity = ApiKeyEntity.find("keyId", parsed.keyId()).firstResult();
        if (entity == null || !entity.active) {
            return null;
        }

        String computed = crypto.hashSecret(parsed.secret());
        if (!computed.equals(entity.keyHash)) {
            return null;
        }

        entity.lastUsedAt = Instant.now();

        QuarkusSecurityIdentity.Builder builder = QuarkusSecurityIdentity.builder()
                .setPrincipal(simplePrincipal("apikey:" + entity.keyId));

        for (String feature : entity.features) {
            builder.addRole(feature);
        }

        return builder.build();
    }

    private Principal simplePrincipal(String name) {
        return () -> name;
    }

    record ParsedKey(String keyId, String secret) {
        static ParsedKey parse(String raw) {
            // Expected: mtk_<keyId>_<secret>
            if (raw == null || !raw.startsWith("mtk_")) {
                return null;
            }
            String[] parts = raw.split("_", 3);
            if (parts.length != 3) {
                return null;
            }
            if (parts[1].isBlank() || parts[2].isBlank()) {
                return null;
            }
            return new ParsedKey(parts[1], parts[2]);
        }
    }
}