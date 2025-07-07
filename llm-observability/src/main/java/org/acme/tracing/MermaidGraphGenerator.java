package org.acme.tracing;

import org.acme.tracing.TraceData.*;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class MermaidGraphGenerator {

    public String generate(ConversationTrace trace) {
        if (trace == null) {
            return "graph TD\n    A[No trace found]";
        }

        StringBuilder sb = new StringBuilder("graph TD\n");
        AtomicInteger nodeCounter = new AtomicInteger(0);

        // User Input
        int userInputNode = nodeCounter.getAndIncrement();
        sb.append(String.format("    N%d[\"User Input: %s\"]\n", userInputNode,
                escape(trace.interactions().get(0).prompt())));

        int lastNode = userInputNode;

        // Input Guardrail Check
        boolean hasInputViolations = trace.violations().stream()
                .anyMatch(v -> v.guardrail().contains("BannedWordGuard"));
        
        int inputGuardrailNode = nodeCounter.getAndIncrement();
        sb.append(String.format("    N%d{Input Guardrail Check}\n", inputGuardrailNode));
        sb.append(String.format("    N%d --> N%d\n", lastNode, inputGuardrailNode));

        if (hasInputViolations) {
            int inputViolationNode = nodeCounter.getAndIncrement();
            sb.append(String.format("    N%d[\"Input Blocked: %s\"]\n", inputViolationNode,
                    escape(getInputViolationMessage(trace))));
            sb.append(String.format("    N%d -->|Fail| N%d\n", inputGuardrailNode, inputViolationNode));
            sb.append(String.format("    N%d --> N%d\n", inputViolationNode, inputGuardrailNode));
            lastNode = inputViolationNode;
        } else {
            // LLM Processing
            int llmProcessingNode = nodeCounter.getAndIncrement();
            sb.append(String.format("    N%d[LLM Processing]\n", llmProcessingNode));
            sb.append(String.format("    N%d -->|Pass| N%d\n", inputGuardrailNode, llmProcessingNode));
            lastNode = llmProcessingNode;

            // Tool Calls
            for (ToolCall toolCall : trace.toolCalls()) {
                int toolCallNeededNode = nodeCounter.getAndIncrement();
                sb.append(String.format("    N%d{{Tool Call Needed?}}\n", toolCallNeededNode));
                sb.append(String.format("    N%d -->|Yes| N%d\n", lastNode, toolCallNeededNode));

                int executeToolNode = nodeCounter.getAndIncrement();
                sb.append(String.format("    N%d[\"Execute Tool: %s(%s)\"]\n", executeToolNode, 
                        toolCall.toolName(), escape(toolCall.params())));
                sb.append(String.format("    N%d --> N%d\n", toolCallNeededNode, executeToolNode));

                int toolResultNode = nodeCounter.getAndIncrement();
                sb.append(String.format("    N%d[\"Tool Result: %s\"]\n", toolResultNode, 
                        escape(toolCall.result())));
                sb.append(String.format("    N%d --> N%d\n", executeToolNode, toolResultNode));

                int llmProcessingAfterTool = nodeCounter.getAndIncrement();
                sb.append(String.format("    N%d[\"LLM Processing Result\"]\n", llmProcessingAfterTool));
                sb.append(String.format("    N%d --> N%d\n", toolResultNode, llmProcessingAfterTool));
                lastNode = llmProcessingAfterTool;
            }

            // Output Guardrail Check
            boolean hasOutputViolations = trace.violations().stream()
                    .anyMatch(v -> v.guardrail().contains("ConcisenessGuard"));
            
            int outputGuardrailNode = nodeCounter.getAndIncrement();
            sb.append(String.format("    N%d{Output Guardrail Check}\n", outputGuardrailNode));
            sb.append(String.format("    N%d --> N%d\n", lastNode, outputGuardrailNode));

            if (hasOutputViolations) {
                int outputViolationNode = nodeCounter.getAndIncrement();
                sb.append(String.format("    N%d[\"Output Reprompt: %s\"]\n", outputViolationNode,
                        escape(getOutputViolationMessage(trace))));
                sb.append(String.format("    N%d -->|Fail| N%d\n", outputGuardrailNode, outputViolationNode));
                // Loop back to LLM processing
                sb.append(String.format("    N%d --> N%d\n", outputViolationNode, llmProcessingNode));
            } else {
                // Final Response
                int finalResponseNode = nodeCounter.getAndIncrement();
                String finalResponse = getFinalResponse(trace);
                sb.append(String.format("    N%d[\"Final Response: %s\"]\n", finalResponseNode,
                        escape(finalResponse)));
                sb.append(String.format("    N%d -->|Pass| N%d\n", outputGuardrailNode, finalResponseNode));
            }
        }

        return sb.toString();
    }

    private String getInputViolationMessage(ConversationTrace trace) {
        return trace.violations().stream()
                .filter(v -> v.guardrail().contains("BannedWordGuard"))
                .findFirst()
                .map(GuardrailViolation::violation)
                .orElse("Input blocked");
    }

    private String getOutputViolationMessage(ConversationTrace trace) {
        return trace.violations().stream()
                .filter(v -> v.guardrail().contains("ConcisenessGuard"))
                .findFirst()
                .map(v -> v.reprompt() != null ? v.reprompt() : v.violation())
                .orElse("Output requires reprompt");
    }

    private String getFinalResponse(ConversationTrace trace) {
        if (trace.interactions().isEmpty()) {
            return "No response";
        }
        
        String response = trace.interactions().get(trace.interactions().size() - 1).response();
        
        // If response is empty but we have tool calls, show tool-based response
        if ((response == null || response.isBlank()) && !trace.toolCalls().isEmpty()) {
            if (trace.toolCalls().size() == 1) {
                ToolCall toolCall = trace.toolCalls().get(0);
                return String.format("Tool result: %s = %s", toolCall.toolName(), toolCall.result());
            } else {
                return String.format("Multiple tool results (last: %s)", 
                        trace.toolCalls().get(trace.toolCalls().size() - 1).result());
            }
        }
        
        return response != null ? response : "No response";
    }

    private String escape(String text) {
        if (text == null)
            return "";
        // Escape quotes and limit length for readability
        String escaped = text.replace("\"", "#quot;");
        return escaped.length() > 100 ? escaped.substring(0, 97) + "..." : escaped;
    }
}