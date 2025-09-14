package com.acme.totp.crypto;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base32;

/**
 * Demo "TAN" that HMACs a canonical string to illustrate dynamic linking.
 * Not a standard. For real systems study OCRA (RFC 6287) and PSD2 SCA guidance.
 */
public final class TanSigner {
    private TanSigner() {
    }

    public static String canonical(String txId, String amount, String currency, String beneficiary) {
        // Stable order and separators; no whitespace; caller pre-normalizes amount
        // format
        return "id=" + txId + ";amt=" + amount + ";cur=" + currency + ";to=" + beneficiary;
    }

    public static String sign(String base32Secret, String canonical, int digits) {
        byte[] key = new Base32().decode(base32Secret);
        byte[] mac = hmac("HmacSHA256", key, canonical.getBytes(StandardCharsets.UTF_8));
        // Reuse HOTP truncation idea for N digits
        int offset = mac[mac.length - 1] & 0x0F;
        int binary = ((mac[offset] & 0x7F) << 24) |
                ((mac[offset + 1] & 0xFF) << 16) |
                ((mac[offset + 2] & 0xFF) << 8) |
                (mac[offset + 3] & 0xFF);
        int code = binary % (int) Math.pow(10, digits);
        String s = Integer.toString(code);
        return "0".repeat(digits - s.length()) + s;
    }

    private static byte[] hmac(String algo, byte[] key, byte[] msg) {
        try {
            Mac mac = Mac.getInstance(algo);
            mac.init(new SecretKeySpec(key, algo));
            return mac.doFinal(msg);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
