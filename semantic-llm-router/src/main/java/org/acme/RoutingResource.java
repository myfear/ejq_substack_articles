package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/chat")
public class RoutingResource {

    @Inject
    SmartChatService chatService;

    @GET
    @Path("/models")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getAvailableModels() {
        return chatService.getAvailableModels();
    }
    
    @GET
    @Path("/models/all")
    @Produces(MediaType.APPLICATION_JSON)
    public String[] getAllSupportedNames() {
        return chatService.getAllSupportedNames();
    }
    
    @GET
    @Path("/models/info")
    @Produces(MediaType.TEXT_PLAIN)
    public String getModelsInfo() {
        StringBuilder info = new StringBuilder("Available Models:\n\n");
        
        for (String modelName : chatService.getAvailableModels()) {
            var modelInfo = chatService.getModelInfo(modelName);
            if (modelInfo != null) {
                info.append(String.format("Model: %s\n", modelName));
                info.append(String.format("  Actual ID: %s\n", modelInfo.actualModelId()));
                info.append(String.format("  Description: %s\n\n", modelInfo.description()));
            }
        }
        
        return info.toString();
    }
    
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String chat(@QueryParam("prompt") String prompt, @QueryParam("model") String model) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Please provide a prompt parameter";
        }
        
        return chatService.chat(model, prompt);
    }
    
    @GET
    @Path("/smart")
    @Produces(MediaType.TEXT_PLAIN)
    public String smartChat(@QueryParam("prompt") String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Please provide a prompt parameter";
        }
        
        return chatService.smartChat(prompt);
    }
    
    @GET
    @Path("/smart/details")
    @Produces(MediaType.TEXT_PLAIN)
    public String smartChatWithDetails(@QueryParam("prompt") String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Please provide a prompt parameter";
        }
        
        var result = chatService.chatWithDetails(prompt);
        return result.getFormattedResult();
    }
    
    @GET
    @Path("/route")
    @Produces(MediaType.TEXT_PLAIN)
    public String routeOnly(@QueryParam("prompt") String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            return "Please provide a prompt parameter";
        }
        
        String selectedModel = chatService.getSelectedModel(prompt);
        var modelInfo = chatService.getModelInfo(selectedModel);
        
        return String.format("Prompt: %s\nSelected model: %s (%s)\nDescription: %s", 
                prompt, selectedModel, modelInfo.actualModelId(), modelInfo.description());
    }
}
