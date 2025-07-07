package org.acme;

import org.acme.tracing.LLMCallTracking;

import dev.langchain4j.agent.tool.Tool;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CalculatorTools {

    @Tool("Calculates the sum of two numbers, 'a' and 'b'.")
    @LLMCallTracking
    public double add(double a, double b) {
        Log.infof("Tool executed: add(%.2f, %.2f)%n", a, b);
        return a + b;
    }

    @Tool("Calculates the difference between two numbers, 'a' and 'b'.")
    @LLMCallTracking
    public double subtract(double a, double b) {
        Log.infof("Tool executed: subtract(%.2f, %.2f)%n", a, b);
        return a - b;
    }
}