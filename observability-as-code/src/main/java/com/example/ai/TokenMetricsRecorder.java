package com.example.ai;

import dev.langchain4j.observability.api.event.AiServiceResponseReceivedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@ApplicationScoped
public class TokenMetricsRecorder {

    private final MeterRegistry registry;
    private final String modelName;
    private final ConcurrentMap<String, Counter> counters = new ConcurrentHashMap<>();

    public TokenMetricsRecorder(
            MeterRegistry registry,
            @ConfigProperty(name = "llm.model.name", defaultValue = "ollama-llama3") String modelName) {
        this.registry = registry;
        this.modelName = modelName;
    }

    private Counter getOrCreateCounter(String name, String modelName) {
        String key = name + ":" + modelName;
        return counters.computeIfAbsent(key, k -> 
            Counter.builder(name)
                    .tag("modelName", modelName)
                    .description("Total number of " + name.replace("llm_token_", "").replace("_tokens_total", ""))
                    .register(registry)
        );
    }

    public void onAiServiceResponseReceived(@Observes AiServiceResponseReceivedEvent event) {
        var response = event.response();
        if (response == null || response.tokenUsage() == null) {
            return;
        }

        var usage = response.tokenUsage();

        getOrCreateCounter("llm_token_input_count_tokens_total", modelName)
                .increment(usage.inputTokenCount());

        getOrCreateCounter("llm_token_output_count_tokens_total", modelName)
                .increment(usage.outputTokenCount());

        getOrCreateCounter("llm_token_count_tokens_total", modelName)
                .increment(usage.totalTokenCount());
    }
}