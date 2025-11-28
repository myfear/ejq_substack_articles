package com.example.entity;

import com.example.json.Views;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

@Entity
public class VideoPost extends Content {

    @JsonView(Views.Public.class)
    public String videoUrl;

    @JsonView(Views.Public.class)
    public int durationSeconds;

    @JsonView(Views.PremiumFeature.class)
    public String bitrate;

    @JsonView(Views.AdminFeature.class)
    public String fsk;

    // THE DYNAMIC CALCULATION
    // Calculated on the fly. Only serialized if the view allows 'PremiumFeature'.
    @JsonView(Views.PremiumFeature.class)
    @JsonProperty("fileSize")
    @Transient // Not in DB
    public String getEstimatedFileSize() {
        if (bitrate != null && "4k".equals(bitrate)) {
            return (durationSeconds * 50) + " MB"; // Mock calculation
        }
        return (durationSeconds * 5) + " MB";
    }
}
