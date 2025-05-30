package com.quarkflix.service;

import com.quarkflix.annotation.AuditLog;
import com.quarkflix.annotation.RequiresAgeVerification;
import com.quarkflix.annotation.TrackPerformance;
import com.quarkflix.annotation.VipAccessRequired;
import com.quarkflix.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class StreamingService {

    private static final Logger LOG = Logger.getLogger(StreamingService.class);

    @AuditLog("User Playback") // Applying the interceptor
    public String playMovie(User user, String movieId) {
        String message = String.format("User %s is now playing movie %s.", user.getUsername(), movieId);
        LOG.info("Core logic: " + message); // Simulate business logic
        return message;
    }

    @AuditLog("Account Access")
    public String getAccountDetails(User user) {
        String message = String.format("Fetching account details for %s.", user.getUsername());
        LOG.info("Core logic: " + message); // Simulate business logic
        return message + " Balance: $10.00";
    }

    // A method without the interceptor for comparison
    public String browseCatalog(User user) {
        String message = String.format("User %s is Browse the catalog.", user.getUsername());
        LOG.info("Core logic: " + message);
        return message;
    }

    // Age verification interceptor applied to a method that requires age
    // verification
    @RequiresAgeVerification(minimumAge = 18) // Must be 18 or older
    @AuditLog("Mature Content Playback") // Also audit this
    public String streamMatureContent(User user, String movieId) {
        String message = String.format("User %s is streaming mature content: %s.", user.getUsername(), movieId);
        LOG.info("Core logic: " + message);
        return message;
    }

    @TrackPerformance // Apply performance tracking
    @AuditLog("Continue Watching List") // Also audit this action
    public String getContinueWatchingList(User user) {
        LOG.infof("Core logic: Fetching continue watching list for %s...", user.getUsername());
        try {
            // Simulate some work
            Thread.sleep(50 + (long) (Math.random() * 100)); // Simulate 50-150ms delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return String.format("Continue Watching List for %s: [MovieA, MovieB, MovieC]", user.getUsername());
    }

    @VipAccessRequired // VipAccessRequired annotation to check VIP status
    @RequiresAgeVerification(minimumAge = 13) // Premieres might also have age ratings
    @TrackPerformance
    @AuditLog("VIP Premiere Playback")
    public String playPremiereMovie(User user, String movieId) {
        String message = String.format("VIP User %s is playing premiere movie: %s (Age %d).",
                user.getUsername(), movieId, user.getAge());
        LOG.info("Core logic: " + message);
        return message;
    }

}