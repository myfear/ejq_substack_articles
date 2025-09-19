package com.ibm.txc.museum.sentiment;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.ibm.txc.museum.domain.Art;
import com.ibm.txc.museum.domain.SentimentVote;

import io.quarkus.hibernate.orm.panache.PanacheQuery;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/analytics")
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsResource {

    @GET
    @Path("/summary")
    public Map<String, Object> summary() {
        Map<String, Object> out = new LinkedHashMap<>();
        List<Art> arts = Art.listAll();
        for (Art a : arts) {
            Map<String, Long> counts = new LinkedHashMap<>();
            for (String label : List.of("very_negative", "negative", "neutral", "positive", "very_positive")) {
                counts.put(label, count(a, label));
            }
            out.put(a.code, counts);
        }
        return out;
    }

    private long count(Art a, String label) {
        PanacheQuery<SentimentVote> q = SentimentVote.find("art=?1 and label=?2", a, label);
        return q.count();
    }
}