package com.example.gists.model;

import java.time.Instant;

public class Gist {
    public String id;
    public String title;
    public String language;
    public String markdown;
    public String html;
    public Instant createdAt;
}