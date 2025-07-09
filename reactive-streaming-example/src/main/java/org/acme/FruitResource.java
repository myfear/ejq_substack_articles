package org.acme;

import org.jboss.resteasy.reactive.RestStreamElementType;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Multi;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/fruits")
public class FruitResource {

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    @RestStreamElementType(MediaType.APPLICATION_JSON)
    public Multi<Fruit> streamFruits() {
        return Panache.withSession(() -> Fruit.listAll())
            .onItem().transformToMulti(Multi.createFrom()::iterable)
            .onItem().castTo(Fruit.class);
    }
}