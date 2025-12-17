package dev.demo.toml.config;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AiConfiguration {

    private String title;
    private String version;
    private String environment;

    private ModelConfig model;
    private PromptsConfig prompts;
    private FeaturesConfig features;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public ModelConfig getModel() {
        return model;
    }

    public void setModel(ModelConfig model) {
        this.model = model;
    }

    public PromptsConfig getPrompts() {
        return prompts;
    }

    public void setPrompts(PromptsConfig prompts) {
        this.prompts = prompts;
    }

    public FeaturesConfig getFeatures() {
        return features;
    }

    public void setFeatures(FeaturesConfig features) {
        this.features = features;
    }

    public static class ModelConfig {
        private String provider;
        private String name;
        private Double temperature;

        @JsonProperty("max_tokens")
        private Integer maxTokens;

        @JsonProperty("timeout_seconds")
        private Integer timeoutSeconds;

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Integer getTimeoutSeconds() {
            return timeoutSeconds;
        }

        public void setTimeoutSeconds(Integer timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
        }
    }

    public static class PromptsConfig {
        private String system;
        private List<PromptExample> examples;

        public String getSystem() {
            return system;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public List<PromptExample> getExamples() {
            return examples;
        }

        public void setExamples(List<PromptExample> examples) {
            this.examples = examples;
        }
    }

    public static class PromptExample {
        private String user;
        private String assistant;

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getAssistant() {
            return assistant;
        }

        public void setAssistant(String assistant) {
            this.assistant = assistant;
        }
    }

    public static class FeaturesConfig {

        @JsonProperty("enable_memory")
        private Boolean enableMemory;

        @JsonProperty("enable_tools")
        private Boolean enableTools;

        @JsonProperty("enable_rag")
        private Boolean enableRag;

        @JsonProperty("log_requests")
        private Boolean logRequests;

        @JsonProperty("log_responses")
        private Boolean logResponses;

        private LimitsConfig limits;

        public Boolean getEnableMemory() {
            return enableMemory;
        }

        public void setEnableMemory(Boolean enableMemory) {
            this.enableMemory = enableMemory;
        }

        public Boolean getEnableTools() {
            return enableTools;
        }

        public void setEnableTools(Boolean enableTools) {
            this.enableTools = enableTools;
        }

        public Boolean getEnableRag() {
            return enableRag;
        }

        public void setEnableRag(Boolean enableRag) {
            this.enableRag = enableRag;
        }

        public Boolean getLogRequests() {
            return logRequests;
        }

        public void setLogRequests(Boolean logRequests) {
            this.logRequests = logRequests;
        }

        public Boolean getLogResponses() {
            return logResponses;
        }

        public void setLogResponses(Boolean logResponses) {
            this.logResponses = logResponses;
        }

        public LimitsConfig getLimits() {
            return limits;
        }

        public void setLimits(LimitsConfig limits) {
            this.limits = limits;
        }
    }

    public static class LimitsConfig {

        @JsonProperty("max_history_messages")
        private Integer maxHistoryMessages;

        @JsonProperty("max_context_length")
        private Integer maxContextLength;

        public Integer getMaxHistoryMessages() {
            return maxHistoryMessages;
        }

        public void setMaxHistoryMessages(Integer maxHistoryMessages) {
            this.maxHistoryMessages = maxHistoryMessages;
        }

        public Integer getMaxContextLength() {
            return maxContextLength;
        }

        public void setMaxContextLength(Integer maxContextLength) {
            this.maxContextLength = maxContextLength;
        }
    }
}