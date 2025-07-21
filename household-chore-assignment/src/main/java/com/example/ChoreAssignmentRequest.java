package com.example;

import java.util.List;
import java.util.Map;

public record ChoreAssignmentRequest(
        Map<String, List<String>> familyPreferences,
        Map<String, List<String>> choreSuitability) {
}