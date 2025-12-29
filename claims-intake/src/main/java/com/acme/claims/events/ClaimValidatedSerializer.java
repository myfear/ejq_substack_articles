package com.acme.claims.events;

import io.quarkus.kafka.client.serialization.ObjectMapperSerializer;

public class ClaimValidatedSerializer extends ObjectMapperSerializer<ClaimValidated> {
}
