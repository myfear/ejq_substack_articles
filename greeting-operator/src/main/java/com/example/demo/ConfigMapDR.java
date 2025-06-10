package com.example.demo;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.javaoperatorsdk.operator.api.reconciler.Context;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;

import java.util.Map;

public class ConfigMapDR extends CRUDKubernetesDependentResource<ConfigMap, Greeting> {
    public ConfigMapDR() {
        super(ConfigMap.class);
    }

    @Override
    protected ConfigMap desired(Greeting greeting, Context<Greeting> context) {
        return new ConfigMapBuilder()
            .withNewMetadata()
            .withName(greeting.getMetadata().getName() + "-config")
            .withNamespace(greeting.getMetadata().getNamespace())
            .endMetadata()
            .withData(Map.of("message", greeting.getSpec().message()))
            .build();
    }
}
