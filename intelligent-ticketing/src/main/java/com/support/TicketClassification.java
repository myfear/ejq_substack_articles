package com.support;

public class TicketClassification {
    private String category;
    private String priority;
    private String sentiment;

    // Default constructor required for JSON deserialization
    public TicketClassification() {
    }

    public TicketClassification(String category, String priority, String sentiment) {
        this.category = category;
        this.priority = priority;
        this.sentiment = sentiment;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getSentiment() {
        return sentiment;
    }

    public void setSentiment(String sentiment) {
        this.sentiment = sentiment;
    }
}