package com.example.adapter;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

@ApplicationScoped
public class AdapterRegistry {

    private final List<RequestAdapter<?>> requestAdapters;
    private final List<ResponseAdapter<?>> responseAdapters;

    @Inject
    public AdapterRegistry(Instance<RequestAdapter<?>> reqs, Instance<ResponseAdapter<?>> resps) {
        this.requestAdapters = reqs.stream().toList();
        this.responseAdapters = resps.stream().toList();
    }

    public <T> RequestAdapter<T> requestAdapterFor(String version, Class<T> targetTypeHint) {
        LocalDate v = LocalDate.parse(version);
        @SuppressWarnings("unchecked")
        Optional<RequestAdapter<T>> best = (Optional<RequestAdapter<T>>) (Optional<?>) requestAdapters.stream()
                .filter(a -> LocalDate.parse(a.version()).compareTo(v) <= 0)
                .sorted(Comparator.comparing(a -> LocalDate.parse(a.version()), Comparator.reverseOrder()))
                .findFirst();
        return best.orElseThrow(() -> new IllegalArgumentException("No RequestAdapter for version " + version));
    }

    public <R> ResponseAdapter<R> responseAdapterFor(String version, Class<R> targetTypeHint) {
        LocalDate v = LocalDate.parse(version);
        @SuppressWarnings("unchecked")
        Optional<ResponseAdapter<R>> best = (Optional<ResponseAdapter<R>>) (Optional<?>) responseAdapters.stream()
                .filter(a -> LocalDate.parse(a.version()).compareTo(v) <= 0)
                .sorted(Comparator.comparing(a -> LocalDate.parse(a.version()), Comparator.reverseOrder()))
                .findFirst();
        return best.orElseThrow(() -> new IllegalArgumentException("No ResponseAdapter for version " + version));
    }
}