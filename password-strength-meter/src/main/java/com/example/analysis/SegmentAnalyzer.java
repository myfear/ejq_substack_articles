package com.example.analysis;

import java.util.ArrayList;
import java.util.List;

public class SegmentAnalyzer {

    public static double pronounceability(String password) {
        List<String> segs = extract(password);
        if (segs.isEmpty())
            return 0.0;
        int pron = 0;

        for (String s : segs)
            if (isPronounceable(s))
                pron++;
        return (double) pron / segs.size();
    }

    private static List<String> extract(String pw) {
        List<String> segs = new ArrayList<>();
        String[] parts = pw.split("[^a-zA-Z]+");
        for (String p : parts)
            if (p.length() >= 3)
                segs.add(p);
        return segs;
    }

    private static boolean isPronounceable(String seg) {
        boolean hasV = seg.matches(".*[aeiou].*");
        boolean hasC = seg.matches(".*[bcdfghjklmnpqrstvwxyz].*");
        if (!hasV || !hasC)
            return false;

        int alt = 0;
        boolean lastV = isVowel(seg.charAt(0));
        for (int i = 1; i < seg.length(); i++) {
            boolean v = isVowel(seg.charAt(i));
            if (v != lastV) {
                alt++;
                lastV = v;
            }
        }
        return alt >= seg.length() * 0.4;
    }

    private static boolean isVowel(char c) {
        return "aeiouAEIOU".indexOf(c) >= 0;
    }
}