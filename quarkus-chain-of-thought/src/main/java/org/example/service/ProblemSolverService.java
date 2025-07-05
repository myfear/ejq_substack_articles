package org.example.service;

import org.example.ChainOfThoughtPipeline;
import org.example.model.ChainOfThoughtResult;
import org.example.steps.FinalAnswerStep;
import org.example.steps.ProblemAnalysisStep;
import org.example.steps.SolutionExecutionStep;
import org.example.steps.SolutionPlanningStep;
import org.example.steps.VerificationStep;

import dev.langchain4j.model.chat.ChatModel;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class ProblemSolverService {

    @Inject
    ChainOfThoughtPipeline pipeline;

    @Inject
    ChatModel mathProblemSolver;


    public ChainOfThoughtResult solve(String input) {
        pipeline = new ChainOfThoughtPipeline(mathProblemSolver)
            .addStep(new ProblemAnalysisStep())
            .addStep(new SolutionPlanningStep())
            .addStep(new SolutionExecutionStep())
            .addStep(new VerificationStep())
            .addStep(new FinalAnswerStep());
        
        return pipeline.execute(input);
    }
}

