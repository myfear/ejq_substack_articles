package com.example.chirper;

import java.time.LocalDateTime;
import java.util.List;

import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ChirpService {

    @Inject
    @Channel("chirp-events")
    Emitter<String> chirpEmitter;

    @Transactional
    public Chirp createChirp(User author, String content) {
        if (content.length() > 280) {
            throw new IllegalArgumentException("Chirp too long! Maximum 280 characters.");
        }

        Chirp chirp = new Chirp();
        chirp.content = content;
        chirp.author = author;
        chirp.createdAt = LocalDateTime.now();
        chirp.persist();

        // Send event to Kafka
        String event = String.format("New chirp by %s: %s", author.username, content);
        chirpEmitter.send(event);

        return chirp;
    }

    public List<Chirp> getAllChirps() {
        return Chirp.findAllOrderedByDate();
    }

    public List<Chirp> getChirpsByUser(User user) {
        return Chirp.findByAuthor(user);
    }

    @Transactional
    public void likeChirp(Long chirpId) {
        Chirp chirp = Chirp.findById(chirpId);
        if (chirp != null) {
            chirp.likes++;
            chirp.persist();
        }
    }
}