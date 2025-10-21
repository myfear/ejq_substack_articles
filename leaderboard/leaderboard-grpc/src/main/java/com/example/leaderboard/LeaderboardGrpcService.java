package com.example.leaderboard;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicReference;

import io.grpc.stub.StreamObserver;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.subscription.MultiEmitter;

@GrpcService
public class LeaderboardGrpcService extends LeaderboardServiceGrpc.LeaderboardServiceImplBase {

    private final Map<String, Integer> scores = new ConcurrentHashMap<>();
    private final ConcurrentSkipListMap<String, Integer> leaderboard = new ConcurrentSkipListMap<>(
            Comparator.reverseOrder());

    private final AtomicReference<MultiEmitter<? super LeaderboardEntry>> broadcasterRef = new AtomicReference<>();
    private final Multi<LeaderboardEntry> broadcaster = Multi.createFrom()
            .emitter(emitter -> broadcasterRef.set(emitter));

    @Override
    public void submitScore(SubmitScoreRequest request, StreamObserver<ScoreResponse> responseObserver) {
        if (request.getPlayer().isBlank() || request.getScore() < 0) {
            responseObserver.onError(io.grpc.Status.INVALID_ARGUMENT
                    .withDescription("Invalid player name or score")
                    .asRuntimeException());
            return;
        }

        scores.put(request.getPlayer(), request.getScore());
        leaderboard.put(request.getPlayer(), request.getScore());

        // Broadcast the new entry to all subscribers
        MultiEmitter<? super LeaderboardEntry> emitter = broadcasterRef.get();
        if (emitter != null) {
            emitter.emit(
                    LeaderboardEntry.newBuilder()
                            .setPlayer(request.getPlayer())
                            .setScore(request.getScore())
                            .build());
        }

        responseObserver.onNext(
                ScoreResponse.newBuilder()
                        .setSuccess(true)
                        .setMessage("Score submitted for " + request.getPlayer())
                        .build());
        responseObserver.onCompleted();
    }

    @Override
    public void watchLeaderboard(com.google.protobuf.Empty request, StreamObserver<LeaderboardEntry> responseObserver) {
        broadcaster.subscribe().with(responseObserver::onNext, responseObserver::onError,
                responseObserver::onCompleted);
    }
}