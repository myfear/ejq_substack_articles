package org.acme.factory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class EndpointClientFactory {

    private final Map<String, HttpClient> clients = new HashMap<>();

    @PostConstruct
    void init() {
        Config cfg = ConfigProvider.getConfig();
        // e.g., endpoints.list=payments,users
        List<String> ids = cfg.getOptionalValue("endpoints.list", String.class)
                .map(s -> Arrays.stream(s.split(",")).map(String::trim).collect(Collectors.toList()))
                .orElseGet(List::of);

        for (String id : ids) {
            String url = cfg.getOptionalValue("endpoints." + id + ".url", String.class)
                    .orElseThrow(() -> new IllegalStateException("Missing URL for " + id));
            clients.put(id, new HttpClient(url));
        }
    }

    public HttpClient client(String id) {
        return clients.get(id);
    }

    public static class HttpClient {
        private final String baseUrl;

        public HttpClient(String baseUrl) {
            this.baseUrl = baseUrl;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public String call(String path) {
            return "GET " + baseUrl + path;
        }
    }
}