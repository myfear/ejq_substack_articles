package org.acme;

import org.jboss.logging.Logger;

import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.OnTextMessage;
import io.quarkus.websockets.next.WebSocket;


@WebSocket(path = "/ws/chat")
public class ChatSocket {

  private final SessionScopeChatAgent agent;

    public ChatSocket(SessionScopeChatAgent agent) {
        this.agent = agent;
    }

private static final Logger log = Logger.getLogger(ChatSocket.class);

    @OnOpen
    public String onOpen() {
         log.infof("WebSocket connection opened");
        return agent.chat("Hello, how can I help you?");
    }

    @OnTextMessage
    public String onMessage(String message) {
        log.infof("Websocket OnTextMessage: %s", message);
        return agent.chat(message);
    }

}
