package com.example.analysis;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SyllableAnalyzer {
    private static final Pattern SYLLABLE_PATTERN = Pattern
            .compile("[bcdfghjklmnpqrstvwxyz]*[aeiou]+[bcdfghjklmnpqrstvwxyz]*", Pattern.CASE_INSENSITIVE);

    public static int countSyllables(String password) {
        String lettersOnly = password.replaceAll("[^a-zA-Z]", " ");
        String[] words = lettersOnly.split("\\s+");
        int total = 0;

        for (String word : words) {
            if (word.length() < 2)
                continue;
            Matcher matcher = SYLLABLE_PATTERN.matcher(word);
            while (matcher.find())
                total++;
        }
        return total;
    }

    public static double getSyllableDensity(String password) {
        int syllables = countSyllables(password);
        int letters = password.replaceAll("[^a-zA-Z]", "").length();
        return letters == 0 ? 0.0 : (double) syllables / letters;
    }

    public static boolean hasTongueTwisters(String password) {
        return password.matches(".*[bcdfghjklmnpqrstvwxyz]{4,}.*") ||
                password.matches(".*[aeiou]{3,}.*");
    }
}