package org.acme.todo;

public final class Slugger {
    private Slugger() {
    }

    public static String slugify(String input) {
        if (input == null)
            return "";
        String s = input.trim().toLowerCase();
        s = s.replaceAll("[^a-z0-9]+", "-");
        s = s.replaceAll("^-+|-+$", "");
        return s.length() > 100 ? s.substring(0, 100) : s;
    }
}