package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.codec.digest.DigestUtils;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FingerprintService {

    public String datasetFingerprint(List<String> records) {
        List<String> sorted = new ArrayList<>(records);
        Collections.sort(sorted);
        String combined = sorted.stream()
                .map(DigestUtils::sha256Hex)
                .collect(Collectors.joining());
        return DigestUtils.sha256Hex(combined);
    }
}