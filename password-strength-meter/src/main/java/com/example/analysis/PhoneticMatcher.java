package com.example.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.language.Metaphone;
import org.apache.commons.codec.language.Soundex;

public class PhoneticMatcher {

    private final Soundex soundex = new Soundex();
    private final Metaphone metaphone = new Metaphone();

    private static final Map<String, Set<String>> WEAK_PATTERNS = loadWeakPatterns();

    private static Map<String, Set<String>> loadWeakPatterns() {
        String[] weak = { "password", "admin", "welcome", "letmein", "sunshine" };
        Map<String, Set<String>> map = new HashMap<>();
        Soundex sx = new Soundex();
        Metaphone mp = new Metaphone();

        for (String w : weak) {
            Set<String> encodings = new HashSet<>();
            encodings.add(sx.encode(w));
            encodings.add(mp.encode(w));
            map.put(w, encodings);
        }
        return map;
    }

    public Result check(String password) {
        String clean = password.replaceAll("[^a-zA-Z]", "");
        if (clean.length() < 3)
            return new Result(false, null, 0);

        String sound = soundex.encode(clean);
        String meta = metaphone.encode(clean);

        for (var entry : WEAK_PATTERNS.entrySet()) {
            if (entry.getValue().contains(sound) || entry.getValue().contains(meta)) {
                int similarity = similarity(clean, entry.getKey());
                return new Result(true, entry.getKey(), similarity);
            }
        }
        return new Result(false, null, 0);
    }

    private int similarity(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++)
            dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++)
            dp[0][j] = j;

        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(
                        dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                        dp[i - 1][j - 1] + cost);
            }
        }
        int distance = dp[s1.length()][s2.length()];
        int max = Math.max(s1.length(), s2.length());
        return (int) ((1.0 - (double) distance / max) * 100);
    }

    public record Result(boolean match, String pattern, int similarity) {
    }
}