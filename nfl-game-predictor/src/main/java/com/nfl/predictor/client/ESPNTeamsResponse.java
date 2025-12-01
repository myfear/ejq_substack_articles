package com.nfl.predictor.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ESPNTeamsResponse {
    public List<Team> teams;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Team {
        public String id;
        public String displayName;
        public String abbreviation;
        public String location;
        public String name;
    }
}

