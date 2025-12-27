package com.mainthread.apikey.keys;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApiKeyCrypto {

    private final SecureRandom secureRandom = new SecureRandom();

    @ConfigProperty(name = "app.apikey.pepper")
    String pepper;

    public GeneratedKey generate() {
        // A short, loggable keyId and a longer secret.
        String keyId = randomUrlSafe(12);
        String secret = randomUrlSafe(32);

        // What clients store and send. What you store is only the hash of the secret.
        String presented = "mtk_" + keyId + "_" + secret;
        return new GeneratedKey(keyId, secret, presented, hashSecret(secret));
    }

    public String hashSecret(String secret) {
        // SHA-256 + pepper is a pragmatic baseline.
        // If your threat model includes offline cracking of weak secrets, use a slow
        // hash (Argon2/bcrypt).
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = (pepper + ":" + secret).getBytes(StandardCharsets.UTF_8);
            byte[] hashed = digest.digest(bytes);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashed);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to hash API key secret", e);
        }
    }

    private String randomUrlSafe(int bytes) {
        byte[] raw = new byte[bytes];
        secureRandom.nextBytes(raw);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    public record GeneratedKey(String keyId, String secret, String presentedKey, String secretHash) {
    }
}