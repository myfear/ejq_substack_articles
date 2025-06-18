package org.acme.wizard.store;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

import org.acme.wizard.model.WizardStateEntity;

import io.quarkus.hibernate.reactive.panache.PanacheRepositoryBase;
import io.smallrye.mutiny.Uni;


@ApplicationScoped
public class WizardStateRepository implements PanacheRepositoryBase<WizardStateEntity, UUID> {
    /**
     * Finds a WizardStateEntity by its ID.
     *
     * @param id The ID of the wizard state.
     * @return A Uni containing the WizardStateEntity if found, or empty if not found.
     */
    public Uni<WizardStateEntity> findById(String id) {
        return find("id", id).firstResult(); 
    }
    /**
     * Deletes a WizardStateEntity by its ID.
     *
     * @param id The ID of the wizard state to delete.
     * @return A Uni containing the number of deleted entities (0 or 1).
     */
    public Uni<Long> deleteById(String id) {
        return delete("id", id);
    }
}