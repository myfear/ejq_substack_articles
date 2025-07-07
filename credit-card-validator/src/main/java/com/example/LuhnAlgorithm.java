package com.example;

public class LuhnAlgorithm {

    public static boolean isValid(String cardNumber) {
        if (cardNumber == null || cardNumber.isBlank())
            return false;
        String cleaned = cardNumber.replaceAll("[\\s-]+", "");
        int sum = 0;
        boolean alt = false;
        for (int i = cleaned.length() - 1; i >= 0; i--) {
            int n = Integer.parseInt(cleaned.substring(i, i + 1));
            if (alt) {
                n *= 2;
                if (n > 9)
                    n = (n % 10) + 1;
            }
            sum += n;
            alt = !alt;
        }
        return (sum % 10 == 0);
    }
}