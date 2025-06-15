package com.example;

import org.jboss.logging.Logger;

import com.example.model.RenderedResponse;
import com.example.service.LearningAssistant;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class LearningResource {

    @Inject
    LearningAssistant.Researcher researcherAI;

    @Inject
    LearningAssistant.Renderer rendererAI;

    @Inject
    ObjectMapper objectMapper;

    private static final Logger LOG = Logger.getLogger(LearningResource.class);

    @POST
    @Path("/ask")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    public String ask(@FormParam("question") String question) throws Exception {
        // 1. Call the Researcher AI to get the content
        String researchResult = researcherAI.research(question);

        // 2. Call the Renderer AI to structure the content
        String jsonResponse = rendererAI.render(researchResult);
        LOG.infof("RAW JSON from Renderer: %s", jsonResponse);

        // 3. Parse the JSON into our POJOs
        RenderedResponse renderedResponse = null;
        try {
            // A simple way to clean potential markdown ```json ``` wrapper
            // String cleanJson = jsonResponse.replace("```json", "").replace("```",
            // "").trim();
            renderedResponse = objectMapper.readValue(jsonResponse, RenderedResponse.class);
            // LOG.infof("Parsed JSON: %s", jsonResponse);
            if (renderedResponse != null && renderedResponse.elements != null) {
                for (com.example.model.UIElement element : renderedResponse.elements) {
                    LOG.infof("Element class: %s, renderHint: %s",
                            element == null ? "null" : element.getClass().getName(),
                            element == null ? "null" : element.renderHint);
                }
            }

        } catch (Exception e) {
            LOG.errorf("Failed to parse JSON: " + jsonResponse, e);
            // If parsing fails, return the raw JSON response because the Guardrail ensures
            // it's valid JSON
            return jsonResponse;
        }

        return renderedResponse.toString();
    }

}