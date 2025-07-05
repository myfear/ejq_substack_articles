package org.example;

import org.example.model.ChainOfThoughtResponse;
import org.example.model.ChainOfThoughtResult;
import org.example.service.ProblemSolverService;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

/**
 * This class is the main entry point for the math problem solver API.
 * It provides two endpoints:
 * 1. /ask/{problem} - Returns detailed JSON with all reasoning steps
 * 2. /ask/{problem}/simple - Returns only the final answer as text
 *
 * Some Examples to Try:
 * 1. The Monty Hall Problem
 * One-shot: "There are 3 doors. Behind one is a car, behind the others are
 * goats. You pick door 1. The host opens door 3, revealing a goat. Should you
 * switch to door 2?"
 * CoT version: "Let's think step by step about the probabilities at each
 * stage..."
 * Correct answer: Yes, you should switch. Switching gives you a 2/3 probability
 * of winning, while staying gives you 1/3.
 * 
 * 2. The Birthday Paradox
 * One-shot: "In a room of 23 people, what's the probability that at least two
 * people share the same birthday?"
 * CoT version: "Let's calculate this step by step using the complement
 * probability..."
 * Correct answer: About 50.7% (surprisingly high for most people's intuition)
 * 
 * 3. Simpson's Paradox Example
 * One-shot: "Treatment A has a 78% success rate overall, Treatment B has 83%.
 * Which is better?"
 * CoT version: "Let's examine the data by subgroups to check for confounding
 * variables..."
 * Correct answer: It depends on the subgroups. Treatment A could actually be
 * better for both severe and mild cases individually, even with a lower overall
 * rate due to treating more severe cases.
 * 
 * 4. The Base Rate Fallacy (Medical Test)
 * One-shot: "A disease affects 1% of the population. A test is 90% accurate. If
 * you test positive, what's the probability you have the disease?"
 * CoT version: "Let's use Bayes' theorem step by step with concrete numbers..."
 * Correct answer: Only about 8.3% (much lower than most people guess due to
 * false positives)
 * 
 * 5. The Secretary Problem (Optimal Stopping)
 * One-shot: "You're interviewing 100 candidates sequentially. You must hire
 * immediately or reject forever. What's the optimal strategy?"
 * CoT version: "Let's derive the optimal stopping rule using the 37% rule..."
 * Correct answer: Reject the first 37 candidates, then hire the next candidate
 * who is better than all previous ones. This gives you about a 37% chance of
 * hiring the best candidate.
 */

@Path("/ask")
public class MathResource {

    @Inject
    ProblemSolverService problemSolverService;

    @Inject
    MathProblemSolver mathProblemSolver;

    @GET
    @Path("/{problem}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChainOfThoughtResponse solveProblem(@PathParam("problem") String problem) {
        ChainOfThoughtResult result = problemSolverService.solve(problem);
        return new ChainOfThoughtResponse(problem, result);
    }

    @GET
    @Path("/{problem}/simple")
    @Produces(MediaType.TEXT_PLAIN)
    public String solveProblemSimple(@PathParam("problem") String problem) {
        ChainOfThoughtResult result = problemSolverService.solve(problem);
        return result.getFinalResult();
    }

    @GET
    @Path("/simple/{problem}")
    @Produces(MediaType.TEXT_PLAIN)
    public String solveWithOneLLMCall(@PathParam("problem") String problem) {
        return mathProblemSolver.solveWithSteps(problem);
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String getHelp() {
        return """
                Chain of Thought Problem Solver API

                Available endpoints:
                • GET /ask/{problem} - Returns detailed JSON with all reasoning steps
                • GET /ask/{problem}/simple - Returns only the final answer as text

                Example:
                • /ask/What is 15 times 23?
                • /ask/Solve for x: 2x + 5 = 15/simple

                The detailed JSON response includes:
                - Original problem
                - Success status
                - Final result
                - Step-by-step reasoning process
                - Timestamp
                """;
    }
}
