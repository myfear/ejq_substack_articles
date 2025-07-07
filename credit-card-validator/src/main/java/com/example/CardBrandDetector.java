package com.example;

public class CardBrandDetector {

    public enum Brand {
        VISA, MASTERCARD, AMERICAN_EXPRESS, DISCOVER, UNKNOWN
    }

    public static Brand detect(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank())
            return Brand.UNKNOWN;
        String cleaned = cardNumber.replaceAll("[\\s-]+", "");

        if (cleaned.startsWith("4"))
            return Brand.VISA;
        if (cleaned.matches("^5[1-5].*"))
            return Brand.MASTERCARD;
        if (cleaned.startsWith("34") || cleaned.startsWith("37"))
            return Brand.AMERICAN_EXPRESS;
        if (cleaned.startsWith("6011") || cleaned.startsWith("65"))
            return Brand.DISCOVER;

        return Brand.UNKNOWN;
    }
}