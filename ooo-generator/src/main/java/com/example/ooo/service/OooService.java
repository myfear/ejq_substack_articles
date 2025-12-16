package com.example.ooo.service;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import com.example.ooo.api.OooRequest;
import com.example.ooo.api.OooResponse;
import com.example.ooo.llm.OooDraft;
import com.example.ooo.llm.OooWriter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OooService {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    @Inject
    OooWriter writer;

    @Inject
    ObjectMapper objectMapper;

    public OooResponse generate(OooRequest request) {
        String raw = writer.draft(
                request.displayName(),
                request.email(),
                request.returnDate().format(ISO),
                request.locale(),
                request.tone().name(),
                emptyToNull(request.backupContactName()),
                emptyToNull(request.backupContactEmail()));

        OooDraft draft = parseOrThrow(raw);

        String subject = sanitizeLine(draft.subject());
        String body = sanitizeBody(draft.body());

        return new OooResponse(subject, body);
    }

    private OooDraft parseOrThrow(String raw) {
        try {
            OooDraft draft = objectMapper.readValue(raw, OooDraft.class);

            if (draft.subject() == null || draft.subject().isBlank()) {
                throw new IllegalArgumentException("Model output missing 'subject'");
            }
            if (draft.body() == null || draft.body().isBlank()) {
                throw new IllegalArgumentException("Model output missing 'body'");
            }
            return draft;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Model output was not valid JSON: " + raw, e);
        }
    }

    private static String sanitizeLine(String s) {
        String v = Objects.toString(s, "").trim();
        v = v.replace("\r", "").replace("\n", " ");
        return v.length() > 140 ? v.substring(0, 140).trim() : v;
    }

    private static String sanitizeBody(String s) {
        String v = Objects.toString(s, "").trim();
        v = v.replace("\r", "");
        if (v.length() > 2000) {
            v = v.substring(0, 2000).trim();
        }
        return v;
    }

    private static String emptyToNull(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}