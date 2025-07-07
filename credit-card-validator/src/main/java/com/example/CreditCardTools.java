package com.example;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CreditCardTools {

    // Inject the secret key for HMAC
    @ConfigProperty(name = "app.secret.hmac-key")
    String hmacSecretKey;

    private enum Brand {
        VISA, MASTERCARD, AMERICAN_EXPRESS, DISCOVER, UNKNOWN
    }

    private Brand detect(String cardNumber) {
        // ... (implementation from Part 1, Step 3)
        if (cardNumber == null || cardNumber.replaceAll("\\s+", "").isEmpty()) {
            return Brand.UNKNOWN;
        }
        String cleanCardNumber = cardNumber.replaceAll("\\s+", "");

        if (cleanCardNumber.startsWith("4")) {
            return Brand.VISA;
        } else if (cleanCardNumber.matches("^5[1-5].*")) {
            return Brand.MASTERCARD;
        } else if (cleanCardNumber.startsWith("34") || cleanCardNumber.startsWith("37")) {
            return Brand.AMERICAN_EXPRESS;
        } else if (cleanCardNumber.startsWith("6011") || cleanCardNumber.startsWith("65")) {
            return Brand.DISCOVER;
        }
        return Brand.UNKNOWN;
    }

    private boolean isLuhnValid(String cardNumber) {
        // ... (implementation from Part 1, Step 2)
        if (cardNumber == null || cardNumber.replaceAll("\\s+", "").isEmpty()) {
            return false;
        }
        String cleanCardNumber = cardNumber.replaceAll("\\s+", "");
        int sum = 0;
        boolean alternate = false;
        for (int i = cleanCardNumber.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleanCardNumber.substring(i, i + 1));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n = (n % 10) + 1;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return (sum % 10 == 0);
    }

    @Tool("Validates a credit card number using the Luhn algorithm and determines its brand.")
    public String validateCreditCard(String creditCardNumber) {
        String cleanedNumber = creditCardNumber.replaceAll("[\\s-]+", "");

        boolean isValid = isLuhnValid(cleanedNumber);
        if (!isValid) {
            return "The provided credit card number '" + creditCardNumber
                    + "' is invalid according to the Luhn algorithm.";
        }

        Brand brand = detect(cleanedNumber);
        return "The credit card number '" + creditCardNumber + "' is valid. The brand is " + brand + ".";
    }

    /**
     * Calculates the expected CVC and validates it along with the expiration date.
     * The creditCardNumber is required to calculate the expected CVC.
     * The expiration date must be in MM/YY or MM/YYYY format.
     */
    @Tool("Checks if a credit card is expired and if its CVC is correct. Requires the full credit card number to validate the CVC.")
    public String validateCvcAndExpiration(String creditCardNumber, String cvc, String expirationDate) {
        // 1. Calculate the expected CVC
        String expectedCvc = calculateExpectedCvc(creditCardNumber, hmacSecretKey);
        Log.infof("Expected CVC: %s", expectedCvc);
        // 2. CVC Check
        if (!expectedCvc.equals(cvc)) {
            return "The provided CVC is incorrect.";
        }

        // 3. Expiration Date Check
        try {
            DateTimeFormatter formatter = expirationDate.length() == 5 ? DateTimeFormatter.ofPattern("MM/yy")
                    : DateTimeFormatter.ofPattern("MM/yyyy");
            YearMonth expiry = YearMonth.parse(expirationDate, formatter);
            // The card is valid through the end of its expiration month.
            if (expiry.isBefore(YearMonth.now())) {
                return "The card is expired.";
            }
        } catch (DateTimeParseException e) {
            return "Invalid expiration date format. Please use MM/YY or MM/YYYY.";
        }

        // 4. Success
        return "The CVC is correct and the card is not expired.";
    }

    private String calculateExpectedCvc(String cardNumber, String secretKey) {
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key_spec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256_HMAC.init(secret_key_spec);

            byte[] hmac_hash = sha256_HMAC
                    .doFinal(cardNumber.replaceAll("[\\s-]+", "").getBytes(StandardCharsets.UTF_8));

            int hash_code = Arrays.hashCode(hmac_hash);
            int cvc_value = Math.abs(hash_code % 1000);

            return String.format("%03d", cvc_value);

        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error calculating CVC", e);
        }
    }

}