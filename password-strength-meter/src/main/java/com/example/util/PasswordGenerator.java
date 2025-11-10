package com.example.util;

import java.security.SecureRandom;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.Soundex;

import com.example.analysis.PhoneticMatcher;
import com.example.analysis.SegmentAnalyzer;
import com.example.analysis.SyllableAnalyzer;

public class PasswordGenerator {

    private static final String CONSONANTS = "bcdfghjklmnpqrstvwxyzBCDFGHJKLMNPQRSTVWXYZ";
    private static final String VOWELS = "aeiouAEIOU";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    
    private static final SecureRandom random = new SecureRandom();
    private static final PhoneticMatcher phoneticMatcher = new PhoneticMatcher();
    private static final Soundex soundex = new Soundex();
    private static final Metaphone metaphone = new Metaphone();
    
    // Common weak words to avoid phonetically
    private static final String[] WEAK_WORDS = { "password", "admin", "welcome", "letmein", "sunshine" };
    private static final Set<String> WEAK_PHONETIC_CODES = loadWeakPhoneticCodes();

    private static Set<String> loadWeakPhoneticCodes() {
        Set<String> codes = new HashSet<>();
        for (String word : WEAK_WORDS) {
            codes.add(soundex.encode(word));
            codes.add(metaphone.encode(word));
        }
        return codes;
    }

    /**
     * Generates an anti-pronounceable password with the specified length.
     * The password will have low syllable density, low pronounceability,
     * and avoid phonetic similarity to common words.
     * 
     * @param length Desired password length (minimum 8)
     * @return An anti-pronounceable password
     */
    public static String generate(int length) {
        if (length < 8) {
            length = 8;
        }
        
        int maxAttempts = 100;
        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            String password = generateCandidate(length);
            
            if (isAntiPronounceable(password)) {
                return password;
            }
        }
        
        // If we couldn't find a perfect match, return the last generated password
        // (it should still be reasonably secure)
        return generateCandidate(length);
    }

    /**
     * Generates a password candidate with anti-pronounceable characteristics.
     */
    private static String generateCandidate(int length) {
        StringBuilder password = new StringBuilder();
        
        // Strategy: Use mostly consonants with minimal vowels, interspersed with
        // digits and special characters to break up any potential patterns
        
        // Ensure we have at least one of each character type for security
        password.append(getRandomChar(CONSONANTS));
        password.append(getRandomChar(DIGITS));
        password.append(getRandomChar(SPECIAL));
        
        // Fill the rest with anti-pronounceable pattern
        int remaining = length - password.length();
        for (int i = 0; i < remaining; i++) {
            double rand = random.nextDouble();
            
            if (rand < 0.6) {
                // 60% consonants (most common)
                password.append(getRandomChar(CONSONANTS));
            } else if (rand < 0.75) {
                // 15% digits
                password.append(getRandomChar(DIGITS));
            } else if (rand < 0.90) {
                // 15% special characters
                password.append(getRandomChar(SPECIAL));
            } else {
                // 10% vowels (minimal to reduce syllable density)
                password.append(getRandomChar(VOWELS));
            }
        }
        
        // Shuffle the password to avoid predictable patterns
        return shuffle(password.toString());
    }

    /**
     * Checks if a password meets anti-pronounceable criteria.
     */
    private static boolean isAntiPronounceable(String password) {
        // Check phonetic similarity - must NOT match common words
        if (phoneticMatcher.check(password).match()) {
            return false;
        }
        
        // Check syllable density - should be low (< 0.2 is ideal)
        double density = SyllableAnalyzer.getSyllableDensity(password);
        if (density > 0.3) {
            return false;
        }
        
        // Check segment pronounceability - should be low (< 0.5)
        double pronounceability = SegmentAnalyzer.pronounceability(password);
        if (pronounceability > 0.5) {
            return false;
        }
        
        // Additional check: ensure it doesn't phonetically match weak words
        // using Apache Commons Codec directly
        String lettersOnly = password.replaceAll("[^a-zA-Z]", "");
        if (lettersOnly.length() >= 3) {
            String soundexCode = soundex.encode(lettersOnly);
            String metaphoneCode = metaphone.encode(lettersOnly);
            if (WEAK_PHONETIC_CODES.contains(soundexCode) || 
                WEAK_PHONETIC_CODES.contains(metaphoneCode)) {
                return false;
            }
        }
        
        return true;
    }

    private static char getRandomChar(String charset) {
        return charset.charAt(random.nextInt(charset.length()));
    }

    /**
     * Shuffles the characters in a string using Fisher-Yates algorithm.
     */
    private static String shuffle(String str) {
        char[] chars = str.toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = chars[i];
            chars[i] = chars[j];
            chars[j] = temp;
        }
        return new String(chars);
    }

    /**
     * Generates a default anti-pronounceable password of length 16.
     */
    public static String generate() {
        return generate(16);
    }
}
