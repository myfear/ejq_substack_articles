package org.acme;

import org.acme.model.State;
import org.acme.model.StateEntity;
import org.acme.store.StateRepository;

import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
@Produces(MediaType.TEXT_HTML)
public class Endpoint {

@Inject
StateRepository repository;


@GET
    @Path("/start")
    public Uni<String> startWizard() {
        State newState = new State();
        newState.setCurrentStep(1); // Initial step for display
        StateEntity newStateEntity = new StateEntity();
        newStateEntity.state = newState;


        return repository.persist(newStateEntity)
                .onItem().transform(wizardId -> {
                        Log.infof("Wizard started with ID: %s", wizardId);
                        return wizardId != null ? wizardId.toString() : null;
                });
    }


}
