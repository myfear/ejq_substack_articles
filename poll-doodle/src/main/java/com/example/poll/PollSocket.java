package com.example.poll;

import java.io.Serializable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkus.websockets.next.OnClose;
import io.quarkus.websockets.next.OnOpen;
import io.quarkus.websockets.next.PathParam;
import io.quarkus.websockets.next.WebSocket;
import io.quarkus.websockets.next.WebSocketConnection;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;

@ApplicationScoped
@WebSocket(path = "/ws/polls/{pollId}")
public class PollSocket {

    private static final Set<WebSocketConnection> sessions = ConcurrentHashMap.newKeySet();

    @Inject
    PollSession pollSession;

    @OnOpen
    public void onOpen(WebSocketConnection connection, @PathParam("pollId") String pollId) {
        pollSession.setPollId(Long.parseLong(pollId));
        sessions.add(connection);
    }

    @OnClose
    public void onClose(WebSocketConnection connection) {
        sessions.remove(connection);
    }

    public static void broadcastUpdate(Long pollId) {
        sessions.stream()
                .forEach(c -> c.sendTextAndAwait("update"));
    }
}

@SessionScoped
class PollSession implements Serializable {
    private Long pollId;

    public Long getPollId() {
        return pollId;
    }

    public void setPollId(Long pollId) {
        this.pollId = pollId;
    }
}