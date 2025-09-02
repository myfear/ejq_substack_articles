package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class FetchService {

    private final HttpClient client = HttpClient.newHttpClient();

    public String fetch(String url) throws IOException, InterruptedException {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "ResilientAI/1.0")
                .GET()
                .build();
        HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
        if (resp.statusCode() / 100 != 2) {
            throw new IOException("Non-2xx from upstream: " + resp.statusCode());
        }
        // Naive text cleanup; real systems would use Tika/Docling.
        return resp.body().replaceAll("\\s+", " ").trim();
    }
}