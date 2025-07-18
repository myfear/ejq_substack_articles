package com.example;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CryptoService {

    // In production, store this securely!
    private static final byte[] KEY = "your-32-byte-long-secret-key-!!!".getBytes(StandardCharsets.UTF_8);
    private static final String ALGO = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;

    public String encrypt(String plaintext) throws Exception {
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, iv));
        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);

        return Base64.getEncoder().encodeToString(result);
    }

    public String decrypt(String base64) throws Exception {
        byte[] input = Base64.getDecoder().decode(base64);

        byte[] iv = new byte[GCM_IV_LENGTH];
        System.arraycopy(input, 0, iv, 0, iv.length);

        Cipher cipher = Cipher.getInstance(ALGO);
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(KEY, "AES"), new GCMParameterSpec(128, iv));

        byte[] decrypted = cipher.doFinal(input, GCM_IV_LENGTH, input.length - GCM_IV_LENGTH);
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}