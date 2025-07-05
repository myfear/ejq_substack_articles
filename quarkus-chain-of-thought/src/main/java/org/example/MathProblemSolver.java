package org.example;

import io.quarkiverse.langchain4j.RegisterAiService;

@RegisterAiService
public interface MathProblemSolver {

    String solveWithSteps(String problem);
}
