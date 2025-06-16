package org.acme;

// This Record models the JSON structure we want from the LLM
public record DecomposedInstructions(
    String plannerInstructions,
    String writerInstructions
) {}
