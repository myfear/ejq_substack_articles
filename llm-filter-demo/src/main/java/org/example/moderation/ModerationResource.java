package org.example.moderation;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/v1/moderate")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ModerationResource {

    @Inject
    BloomFilterService bloomFilterService;

    @Inject
    LLMModerator llmModerator;

    public record ModerationRequest(String text) {
    }

    public record ModerationResponse(String text, String status, String checkedBy) {
    }

    @POST
    public ModerationResponse moderate(ModerationRequest request) {
        if (!bloomFilterService.mightContainProblematicNgram(request.text())) {
            return new ModerationResponse(request.text(), "SAFE", "bloom_filter");
        }

        String llmResult = llmModerator.moderate(request.text());
        return new ModerationResponse(request.text(), llmResult.trim(), "llm_model");
    }
}