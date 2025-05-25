package com.resistance;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/people")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Transactional
public class PersonResource {

    @PersistenceContext
    EntityManager em;

    @POST
    public Response create(Person p) {
        em.persist(p);
        return Response.status(Response.Status.CREATED).entity(p).build();
    }

    @GET
    public List<Person> all() {
        return em.createQuery("from Person", Person.class).getResultList();
    }
}
