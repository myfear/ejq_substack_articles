package org.acme.wizard.store;

import jakarta.enterprise.context.ApplicationScoped;
import org.acme.wizard.model.WizardStateEntity;

import io.quarkus.hibernate.reactive.panache.PanacheRepository;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;


@ApplicationScoped
public class WizardStateRepository implements PanacheRepository<WizardStateEntity> {

    
    public Uni<WizardStateEntity> findById(String id) {
        Log.infof("findById called with id: %s", id);
        return find("id", id).firstResult();
        
    }
    public Uni<Long> deleteById(String id) {
        return delete("id", id);
    }
}