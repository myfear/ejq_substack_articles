package com.example.progress;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProgressEvent {
    @JsonProperty("step")
    public String step;
    
    @JsonProperty("percent")
    public int percent;
    
    @JsonProperty("detail")
    public String detail; // optional small text payload

    public ProgressEvent() {
    }

    public ProgressEvent(String step, int percent, String detail) {
        this.step = step;
        this.percent = percent;
        this.detail = detail;
    }
}
