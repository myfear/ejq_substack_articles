package com.acme.totp.crypto;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.time.Instant;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

/**
 * Minimal TOTP implementation following RFC 6238.
 * Do not use in production. This is for educational purposes only.
 */
public final class Totp {

    private Totp() {
    }

    /**
     * Generate a TOTP code for the given secret and timestamp.
     *
     * @param base32Secret  Base32-encoded shared secret
     * @param epochSeconds  current UNIX time in seconds
     * @param digits        number of digits in the OTP (e.g. 6)
     * @param periodSeconds step size in seconds (usually 30)
     * @param hmacAlgo      HMAC algorithm (HmacSHA1, HmacSHA256, HmacSHA512)
     */
    public static String generate(String base32Secret, long epochSeconds,
            int digits, int periodSeconds, String hmacAlgo) {
        long counter = Math.floorDiv(epochSeconds, periodSeconds);
        return hotp(base32Secret, counter, digits, hmacAlgo);
    }

    /**
     * Verify a candidate TOTP code against the shared secret and current time.
     *
     * @param base32Secret  Base32-encoded secret
     * @param code          user-provided code
     * @param digits        expected number of digits
     * @param periodSeconds time step size
     * @param window        number of steps of clock drift to tolerate (+/-)
     * @param hmacAlgo      HMAC algorithm
     * @param epochSeconds  current UNIX time
     */
    public static boolean verify(String base32Secret, String code, int digits,
            int periodSeconds, int window, String hmacAlgo,
            long epochSeconds) {
        for (int i = -window; i <= window; i++) {
            long when = epochSeconds + (long) i * periodSeconds;
            if (generate(base32Secret, when, digits, periodSeconds, hmacAlgo).equals(code)) {
                return true;
            }
        }
        return false;
    }

    /**
     * HOTP core algorithm (RFC 4226).
     */
    public static String hotp(String base32Secret, long counter, int digits, String hmacAlgo) {
        byte[] key = new Base32().decode(base32Secret);
        byte[] msg = ByteBuffer.allocate(8).putLong(counter).array();
        byte[] mac = hmac(hmacAlgo, key, msg);

        // Dynamic truncation
        int offset = mac[mac.length - 1] & 0x0F;
        int binary = ((mac[offset] & 0x7F) << 24) |
                ((mac[offset + 1] & 0xFF) << 16) |
                ((mac[offset + 2] & 0xFF) << 8) |
                (mac[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, digits);
        String s = Integer.toString(otp);
        return "0".repeat(digits - s.length()) + s;
    }

    private static byte[] hmac(String algo, byte[] key, byte[] msg) {
        try {
            Mac mac = Mac.getInstance(algo);
            mac.init(new SecretKeySpec(key, algo));
            return mac.doFinal(msg);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("HMAC algorithm not available", e);
        }
    }

    /**
     * Build an otpauth:// URI that authenticator apps understand.
     */
    public static String otpauthUri(String issuer, String account, String base32Secret,
            int digits, int periodSeconds, String algo) {
        String label = url(issuer) + ":" + url(account);
        return "otpauth://totp/" + label +
                "?secret=" + base32Secret +
                "&issuer=" + url(issuer) +
                "&algorithm=" + url(algo) +
                "&digits=" + digits +
                "&period=" + periodSeconds;
    }

    private static String url(String s) {
        return java.net.URLEncoder.encode(s, StandardCharsets.UTF_8);
    }

    public static long now() {
        return Instant.now().getEpochSecond();
    }
}
