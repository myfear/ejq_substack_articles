package com.example;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.security.SecureRandom;
import java.util.Optional;

import io.quarkus.cache.CacheResult;

@ApplicationScoped
public class ShortenerService {

    private static final String ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final int KEY_LENGTH = 6;
    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public ShortLink createShortLink(String originalUrl) {
        String key;
        do {
            key = generateKey();
        } while (ShortLink.findByKey(key) != null);

        ShortLink link = new ShortLink();
        link.originalUrl = originalUrl;
        link.key = key;
        link.persist();
        return link;
    }

    @CacheResult(cacheName = "urls")
    public Optional<String> getOriginalUrl(String key) {
        return Optional.ofNullable(ShortLink.findByKey(key))
                .map(link -> link.originalUrl);
    }

    private String generateKey() {
        StringBuilder sb = new StringBuilder(KEY_LENGTH);
        for (int i = 0; i < KEY_LENGTH; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}