package org.acme.tracing;

import jakarta.enterprise.context.RequestScoped;

@RequestScoped
public class RequestCorrelation {
    private String conversationId;

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }
}
