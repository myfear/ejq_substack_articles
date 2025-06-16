package org.acme;

import org.jboss.logging.Logger;

import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.service.AiServices;
import io.quarkiverse.langchain4j.ModelName;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class AgentSpawner {

    @Inject
    InstructionGeneratorAgent instructionGenerator;

    // Inject the default model (phi3)
    @Inject
    ChatModel defaultModel;

    // Inject the model named "creative" (gemma:2b)
    @Inject
    @ModelName("creative")
    ChatModel creativeModel;

    private static final Logger LOG = Logger.getLogger(AgentSpawner.class);

    public String spawnAndExecute(String highLevelGoal) {

        // 1. Generate the decomposed instructions as a structured object
        LOG.infof("Generating decomposed instructions for goal: %s", highLevelGoal);
        DecomposedInstructions instructions = instructionGenerator.generateInstructions(highLevelGoal);
        LOG.infof("Generated Planner Instructions: %s", instructions.plannerInstructions());
        LOG.infof("Generated Writer Instructions: %s", instructions.writerInstructions());

        // 2. Spawn Agent 1 (Planner) with the default (phi3) model
        LOG.infof("Spawning PLANNER agent with model: %s", defaultModel.toString());
        TaskExecutionAgent plannerAgent = AiServices.builder(TaskExecutionAgent.class)
                .chatModel(defaultModel)
                .build();
        String plan = plannerAgent.executeTask(highLevelGoal, instructions.plannerInstructions());
        LOG.info("Planner Agent Output (The Plan):\n" + plan);

        // 3. Spawn Agent 2 (Writer) with the "creative" (gemma:2b) model
        LOG.infof("Spawning WRITER agent with model: %s", creativeModel.toString());
        TaskExecutionAgent writerAgent = AiServices.builder(TaskExecutionAgent.class)
                .chatModel(creativeModel)
                .build();

        // We use the original goal as the main detail, but provide the plan from the
        // first agent as context.
        String writerTaskDetails = String.format(
                "The user wants to achieve this goal: '%s'. Your task is to write the final content based on the following plan:\n%s",
                highLevelGoal, plan);

        String finalContent = writerAgent.executeTask(writerTaskDetails, instructions.writerInstructions());
        LOG.infof("Writer Agent Output (Final Content): %s", finalContent);

        return finalContent;

    }
}