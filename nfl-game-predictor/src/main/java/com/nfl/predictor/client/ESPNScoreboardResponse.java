package com.nfl.predictor.client;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ESPNScoreboardResponse {
    public List<Event> events;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {
        public String id;
        public String date;
        public String name;
        public String shortName;
        
        // ESPN API returns "competitions" as an array (plural)
        public List<Competition> competitions;
        
        /**
         * Get the competition data from the competitions array
         * ESPN typically has one competition per event, so we return the first one
         */
        @JsonIgnore
        public Competition getCompetition() {
            if (competitions != null && !competitions.isEmpty()) {
                return competitions.get(0);
            }
            return null;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Competition {
            public String id;
            public String date;
            public List<Competitor> competitors;
            public Status status;
            public Boolean completed; // ESPN also provides this field directly
            
            // Helper method to check if competition is completed
            // Check both the direct completed field and status.type.completed
            public boolean isCompleted() {
                if (completed != null && completed) {
                    return true;
                }
                return status != null && status.type != null && status.type.completed;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Status {
                public StatusType type;
                
                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class StatusType {
                    public int id;
                    public String name;
                    public String state;
                    public boolean completed;
                }
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Competitor {
                public String id;
                public Team team;
                public String score;
                public String homeAway;
                public boolean winner;

                @JsonIgnoreProperties(ignoreUnknown = true)
                public static class Team {
                    public String id;
                    public String displayName;
                    public String abbreviation;
                    public String name;
                    public String location;
                }
            }
        }
    }
}