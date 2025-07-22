package org.acme.diary;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CryptoService {

    private static final String ENCRYPTION_ALGO = "AES/GCM/NoPadding";
    private static final int IV_SIZE = 12; // recommended for GCM
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec key;

    public CryptoService(@ConfigProperty(name = "encryption.secret") String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key.trim());
        this.key = new SecretKeySpec(decodedKey, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO, "BC");
            byte[] iv = new byte[IV_SIZE];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, iv.length);
            System.arraycopy(ciphertext, 0, result, iv.length, ciphertext.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new IllegalStateException("Encryption failed", e);
        }
    }

    public String decrypt(String base64Encrypted) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Encrypted);
            byte[] iv = Arrays.copyOfRange(data, 0, IV_SIZE);
            byte[] ciphertext = Arrays.copyOfRange(data, IV_SIZE, data.length);

            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGO, "BC");
            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Decryption failed", e);
        }
    }
}