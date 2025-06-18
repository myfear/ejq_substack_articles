package org.acme.wizard.store;

import io.quarkus.cache.CacheResult;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.logging.Log;
import io.quarkus.hibernate.reactive.panache.Panache; // For reactive transactions
import io.quarkus.hibernate.reactive.panache.common.WithTransaction;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.acme.wizard.model.WizardState;
import org.acme.wizard.model.WizardStateEntity;
import io.smallrye.mutiny.Uni;
import java.util.Optional;

@ApplicationScoped
public class WizardStateStore {

    @Inject
    WizardStateRepository repository; 

    private static final String CACHE_NAME = "wizard-states";

    /**
     * Retrieves wizard state by ID from cache or database.
     * The result is cached.
     * 
     * @param wizardIdStr The string UUID of the wizard.
     * @return An Optional containing the WizardState if found, empty otherwise.
     */
    @CacheResult(cacheName = CACHE_NAME)
    public Uni<Optional<WizardState>> getWizardState(String wizardIdStr) {
        Log.infof("Attempting to load wizard state for ID: %s from DB.", wizardIdStr);

        return Panache.withTransaction(() -> repository.findById(wizardIdStr))
                .onItem().transform(entity -> {
                    if (entity == null) {
                        return Optional.<WizardState>empty();
                    }
                    return Optional.of(entity.getWizardState());
                })
                .onFailure()
                .invoke(e -> Log.errorf(e, "Error fetching wizard state from DB for ID: %s", wizardIdStr));
    }

    /**
     * Saves or updates wizard state in the database.
     * Invalidates the cache for the specific wizard ID.
     * 
     * @param wizardIdStr The string UUID of the wizard. If null/empty/invalid, a
     *                    new ID is generated.
     * @param state       The WizardState object to save.
     * @return The UUID string of the saved wizard state.
     */
    @CacheInvalidate(cacheName = CACHE_NAME)
    public Uni<String> saveWizardState(String wizardIdStr, WizardState state) {

        if (wizardIdStr == null || wizardIdStr.isEmpty()) {
            // Parse the provided ID
            return createNewWizardState(state);
        }

        return Panache.withSession(() -> repository.findById(wizardIdStr)
                .onItem().transformToUni(entity -> {
                    WizardStateEntity entityToPersist = entity != null ? entity : new WizardStateEntity();
                    if (entity == null) {
                        entityToPersist.setId(wizardIdStr); // Set the ID if new entity
                    }
                    entityToPersist.setWizardState(state); // This sets currentStep and also sets wizardState
                    return repository.persist(entityToPersist); // Returns Uni<Void> for persist
                })
                .replaceWith(wizardIdStr) // Chain to return the wizardId string
        )
                .onFailure().invoke(e -> Log.errorf(e, "Failed to save wizard state for ID: %s", wizardIdStr));
    }

    /**
     * Creates a brand new wizard state and saves it to the database.
     * 
     * @param initialState The initial WizardState object.
     * @return The UUID string of the newly created wizard state.
     */
    @WithTransaction
    public Uni<String> createNewWizardState(WizardState initialState) {
        WizardStateEntity entity = new WizardStateEntity();
        entity.setWizardState(initialState);

        return repository.persist(entity)
                .onItem().invoke(persistedEntity -> Log.infof("createNewWizardState %s", persistedEntity.getId()))
                .onItem().transform(persistedEntity -> persistedEntity.getId());
    }

    /**
     * Deletes a wizard state from the database.
     * Invalidates the cache for the specific wizard ID.
     * 
     * @param wizardIdStr The string UUID of the wizard to delete.
     */
    @WithTransaction
    @CacheInvalidate(cacheName = CACHE_NAME)
    public Uni<Void> deleteWizardState(String wizardIdStr) {

        return Panache.withSession(() -> repository.deleteById(wizardIdStr))
                .onItem().invoke(deleted -> {
                    if (deleted != null && deleted > 0)
                        Log.debugf("Deleted wizard state for ID: %s from DB.", wizardIdStr);
                    else
                        Log.warnf("Wizard state with ID: %s not found for deletion.", wizardIdStr);
                })
                .onFailure().invoke(e -> Log.errorf(e, "Error deleting wizard state for ID: %s", wizardIdStr))
                .replaceWithVoid();

    }
}