package org.acme.critic.ai;

import dev.langchain4j.data.image.Image;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface CompositionCritic {
    @SystemMessage("""
            You are a master art critic with an expert eye for visual composition.
            Your task is to provide a concise yet insightful critique of the provided image's structure.

            Focus entirely on the principles of composition.
            Do not analyze the subject matter, emotional tone, or color palette,
            except where they directly impact visual weight and flow.

            In your analysis, please evaluate:
            - **Balance and Visual Weight:** Is the composition balanced? Symmetrically or asymmetrically?
            - **Leading Lines and Flow:** Where does the eye travel? Is the path clear and intentional?
            - **Rule of Thirds & Golden Ratio:** Are key elements placed effectively according to these principles?
            - **Framing and Negative Space:** How are the edges of the frame and empty spaces used to enhance the composition?

            Please structure your output into two brief sections:
            1.  **Compositional Strengths:**
            2.  **Areas for Improvement:**
                            """)
    @UserMessage("Describe this image.")
    String critiqueComposition(Image image);
}
