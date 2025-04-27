package org.acme.util;

import java.util.regex.Pattern;

public class TypeInference {

    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?\\d+(\\.\\d+)?");

    public static String inferType(String value) {
        if (value == null || value.isBlank()) {
            return "TEXT"; // Default fallback
        }
        if (NUMBER_PATTERN.matcher(value).matches()) {
            return value.contains(".") ? "DOUBLE PRECISION" : "INTEGER";
        }
        return "TEXT";
    }
}
