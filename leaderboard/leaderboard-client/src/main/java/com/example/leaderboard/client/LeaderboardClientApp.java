package com.example.leaderboard.client;

import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import com.example.leaderboard.LeaderboardEntry;
import com.example.leaderboard.LeaderboardServiceGrpc;
import com.example.leaderboard.MutinyLeaderboardServiceGrpc;
import com.example.leaderboard.SubmitScoreRequest;
import com.google.protobuf.Empty;

import io.quarkus.grpc.GrpcClient;
import io.quarkus.logging.Log;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import io.smallrye.mutiny.Multi;
import jakarta.inject.Inject;

@QuarkusMain
public class LeaderboardClientApp implements QuarkusApplication {

    @Inject
    @GrpcClient("leaderboard")
    LeaderboardServiceGrpc.LeaderboardServiceBlockingStub blockingStub;

    @Inject
    @GrpcClient("leaderboard")
    MutinyLeaderboardServiceGrpc.MutinyLeaderboardServiceStub reactiveStub;

    @Override
    public int run(String... args) throws Exception {
        System.out.println("Connecting to leaderboard service...");

        listenToLeaderboard();

        String[] players = { "Alice", "Bob", "Carol", "Dave" };
        var random = new Random();

        for (String player : players) {
            int score = random.nextInt(500);
            blockingStub.submitScore(
                    SubmitScoreRequest.newBuilder()
                            .setPlayer(player)
                            .setScore(score)
                            .build());
            Log.infof("Submitted score for %s: %d%n", player, score);
            Thread.sleep(1000);
        }

        Thread.sleep(5000);
        Log.infof("Done sending scores. Press Ctrl+C to exit.");
        return 0;
    }

    private void listenToLeaderboard() {
        CountDownLatch latch = new CountDownLatch(1);

        Multi<LeaderboardEntry> leaderboardStream = reactiveStub.watchLeaderboard(Empty.getDefaultInstance());

        leaderboardStream
                .onItem()
                .invoke(entry -> Log.infof("LIVE UPDATE: %s => %d%n", entry.getPlayer(), entry.getScore()))
                .onFailure().invoke(throwable -> {
                    Log.errorf("Stream error: " + throwable.getMessage());
                    latch.countDown();
                })
                .onCompletion().invoke(() -> {
                    Log.infof("Stream closed.");
                    latch.countDown();
                })
                .subscribe().with(
                        entry -> {
                        }, // onNext is handled by onItem().invoke above
                        throwable -> {
                        } // onError is handled by onFailure().invoke above
                );

        new Thread(() -> {
            try {
                latch.await(60, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
}