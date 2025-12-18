package com.acme;

// Static import for the generated functions
import static whisper.ffi.whisper_h.whisper_free;
import static whisper.ffi.whisper_h.whisper_full;
import static whisper.ffi.whisper_h.whisper_full_default_params;
import static whisper.ffi.whisper_h.whisper_full_get_segment_text;
import static whisper.ffi.whisper_h.whisper_full_n_segments;
import static whisper.ffi.whisper_h.whisper_init_from_file;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class WhisperService {

    // UPDATE THIS PATH
    private static final String MODEL_PATH = "/Users/meisele/Projects/whisper.cpp/models/ggml-base.en.bin";

    // "Greedy" sampling strategy is enum value 0 in whisper.h
    private static final int STRATEGY_GREEDY = 0;

    private MemorySegment ctx;

    @PostConstruct
    void init() {
        
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment modelPath = arena.allocateFrom(MODEL_PATH);

            ctx = whisper_init_from_file(modelPath);

            if (ctx.equals(MemorySegment.NULL)) {
                throw new IllegalStateException("Failed to initialize Whisper context");
            }
            System.out.println("Whisper initialized");
        }
    }

    public synchronized String transcribe(float[] audioData) {
        try (Arena arena = Arena.ofConfined()) {

            // FIX 1: Pass 'arena' as the first argument.
            // Because the C function returns a struct by value, Java needs
            // an allocator to know where to store that struct memory.
            MemorySegment params = whisper_full_default_params(arena, STRATEGY_GREEDY);

            // FIX 2: Use allocateFrom (Java 22+) for cleaner array copy
            MemorySegment audioBuffer = arena.allocateFrom(ValueLayout.JAVA_FLOAT, audioData);

            int result = whisper_full(
                    ctx,
                    params,
                    audioBuffer,
                    audioData.length);

            if (result != 0) {
                return "Inference failed with code: " + result;
            }

            StringBuilder text = new StringBuilder();

            // These functions require the new jextract command above
            int segments = whisper_full_n_segments(ctx);

            for (int i = 0; i < segments; i++) {
                MemorySegment segment = whisper_full_get_segment_text(ctx, i);

                // Read the C String from the pointer
                text.append(segment.getString(0));
            }

            return text.toString().trim();
        }
    }

    @PreDestroy
    void shutdown() {
        if (ctx != null && !ctx.equals(MemorySegment.NULL)) {
            whisper_free(ctx);
            System.out.println("Whisper context freed");
        }
    }
}