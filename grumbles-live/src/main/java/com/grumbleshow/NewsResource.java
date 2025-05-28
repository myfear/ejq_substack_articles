package com.grumbleshow;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.jboss.logging.Logger; // Using JBoss Logging, which Quarkus is configured with

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Path("/news")
public class NewsResource {

    private static final Logger LOG = Logger.getLogger(NewsResource.class);

    @Inject
    SarcasticAnchorAiService anchorService; // Our AI service, ready to go!

    @CheckedTemplate(requireTypeSafeExpressions = false)
    public static class Templates {
        public static native TemplateInstance grumbles(); 
    }


    // Inject the Qute template.
    // By convention, Quarkus looks for 'grumbles.html' in 'templates/NewsResource/'
    // because this class is NewsResource and the template is named grumbles.
    // @Inject
    // Template grumbles;

    @GET
    @Path("/grumbles") // This is the main endpoint for our web page
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getGrumblesPage(
            @QueryParam("topic") String topic,
            @QueryParam("sessionId") String sessionId) {

        Map<String, Object> data = new HashMap<>(); // Data to pass to the Qute template
        String currentSessionId = sessionId;
        String grumblesSarcasticResponse = null;
        String errorMessage = null;

        // Initialize or reuse sessionId. This is key for conversation memory.
        if (currentSessionId == null || currentSessionId.isBlank()) {
            currentSessionId = UUID.randomUUID().toString();
            LOG.infof("New session initiated with ID: %s", currentSessionId);
        }
        data.put("sessionId", currentSessionId);

        // If a topic was submitted, let's get Grumbles' "hot take"
        if (topic != null && !topic.isBlank()) {
            LOG.infof("Topic received: '%s' for session ID: %s", topic, currentSessionId);
            data.put("topic", topic); // Pass the submitted topic back to the template
            try {
                // This is where we call our AI service!
                grumblesSarcasticResponse = anchorService.deliverNews(currentSessionId, topic);
                LOG.infof("Grumbles' response for session %s: '%s'", currentSessionId, grumblesSarcasticResponse);
            } catch (Exception e) {
                LOG.errorf(e, "Grumbles had a meltdown for topic '%s', session '%s'", topic, currentSessionId);
                // A suitably sarcastic error message
                errorMessage = "It appears Grumbles' circuits are currently sizzling with disdain, or perhaps a genuine error. He muttered something about 'incompetent handlers' and 'the futility of digital existence.' Try again when he's less... volatile.";
            }
        } else if (topic != null && topic.isBlank()){ // Topic submitted but was empty
             grumblesSarcasticResponse = "Ah, a blank topic. You're asking for my profound insights on... *nothing*? Truly avant-garde. Or perhaps your fingers merely slipped into the void. Equally riveting.";
             data.put("topic", ""); // Keep topic in data map
        } else {
            // This is the initial page load (no topic submitted yet).
            LOG.info("Grumbles' stage is set. Awaiting a topic to reluctantly address.");
        }

        data.put("grumblesResponse", grumblesSarcasticResponse);
        data.put("error", errorMessage);

        // Render the 'grumbles.html' template with the prepared data
        return Templates.grumbles().data(data);
    }
}