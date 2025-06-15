package com.example.model;

// Concrete data classes
public class TextElement extends UIElement {
    public Data data;
    public TextElement() {}
    public static class Data {
        public String title;
        public String text;
    }
}
