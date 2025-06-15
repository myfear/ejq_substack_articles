package com.example.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

// This class holds the list of elements to be rendered.
public class RenderedResponse {
    public java.util.List<UIElement> elements;

    @Override
    public String toString() {
        try {
            return new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "{error: 'Failed to serialize RenderedResponse'}";
        }
    }
}
