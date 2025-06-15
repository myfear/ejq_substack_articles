package com.example.model;

public class Article extends UIElement {
    public Data data;
    public Article() {}
    public static class Data {
        public String title;
        public String url;
    }
}