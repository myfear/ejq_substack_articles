package com.example.model;

import java.util.List;

public class ListElement extends UIElement {
    public Data data;

    public ListElement() {}

    public static class Data {
        public String title;
        public List<String> items;
    }
}
