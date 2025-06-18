package org.acme.store;

import java.util.UUID;

import org.acme.model.StateEntity;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StateRepository implements PanacheRepositoryBase<StateEntity, UUID> {
}
