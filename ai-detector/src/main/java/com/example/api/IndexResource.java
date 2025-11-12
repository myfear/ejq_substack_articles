package com.example.api;

import java.text.DecimalFormat;
import java.util.LinkedHashMap;
import java.util.Map;

import com.example.service.AnalysisService;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class IndexResource {

    private static final DecimalFormat DF_2 = new DecimalFormat("#0.00");
    private static final DecimalFormat DF_1 = new DecimalFormat("#0.0");

    @Inject
    Template index;

    @Inject
    AnalysisService service;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {
        return index.data("result", null).data("text", "");
    }

    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance analyze(@FormParam("text") String text) {
        if (text == null || text.isBlank()) {
            return index.data("result", null).data("text", text != null ? text : "");
        }
        Map<String, Object> result = service.analyze(text);
        Map<String, Object> formattedResult = formatResult(result);
        return index.data("result", formattedResult).data("text", text);
    }

    private Map<String, Object> formatResult(Map<String, Object> result) {
        Map<String, Object> formatted = new LinkedHashMap<>(result);

        // Keep raw score for progress bar calculation
        Double rawScore = result.get("ai_score") instanceof Double ? (Double) result.get("ai_score") : null;
        if (rawScore != null) {
            formatted.put("ai_score_raw", rawScore);
            formatted.put("ai_score_percent", (int) (rawScore * 100));
        }

        // Format double values for display
        if (result.get("ai_score") instanceof Double) {
            formatted.put("ai_score", DF_2.format((Double) result.get("ai_score")));
        }
        if (result.get("mean_sentence_length") instanceof Double) {
            formatted.put("mean_sentence_length", DF_1.format((Double) result.get("mean_sentence_length")));
        }
        if (result.get("burstiness") instanceof Double) {
            formatted.put("burstiness", DF_2.format((Double) result.get("burstiness")));
        }
        if (result.get("lexical_diversity") instanceof Double) {
            formatted.put("lexical_diversity", DF_2.format((Double) result.get("lexical_diversity")));
        }
        if (result.get("punctuation_density") instanceof Double) {
            formatted.put("punctuation_density", DF_2.format((Double) result.get("punctuation_density")));
        }
        if (result.get("semantic_bias") instanceof Double) {
            formatted.put("semantic_bias", DF_2.format((Double) result.get("semantic_bias")));
        }
        if (result.get("perplexity_proxy") instanceof Double) {
            formatted.put("perplexity_proxy", DF_2.format((Double) result.get("perplexity_proxy")));
        }

        return formatted;
    }
}