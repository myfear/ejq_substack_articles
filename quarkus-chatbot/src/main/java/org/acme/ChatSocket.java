package org.acme;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;
import io.smallrye.mutiny.Multi;

@WebSocket(path = "/ws/chat")
public class ChatSocket {

    private final SessionScopeChatAgent agent;

    public ChatSocket(SessionScopeChatAgent agent) {
        this.agent = agent;
    }

    @OnOpen
    public Multi<String> onOpen() {
        return Multi.createFrom().item("Connection opened");
    }

    @OnTextMessage
    public Multi<String> onMessage(String message) {
        return agent.chat(message);
    }

}
