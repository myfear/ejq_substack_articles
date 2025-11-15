package com.example.wordcloud.api;

public class CloudParams {
    public int width = 900;
    public int height = 600;
    public int maxWords = 60;
    public boolean rotateSome = true; // ~30% vertical
    public double rotateProb = 0.25; // 0..1
    public String fontFamily = "IBM Plex Sans";
    public int minFont = 12;
    public int maxFont = 72;
    public boolean localRewordle = true; // boundary compaction
    public long seed = 42L; // reproducibility
}