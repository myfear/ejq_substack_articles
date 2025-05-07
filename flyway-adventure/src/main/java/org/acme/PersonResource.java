package org.acme;

import java.util.List;

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
public class PersonResource {

    @POST
    @Transactional
    public Response create(Person person) {
        person.persist();
        return Response.status(Response.Status.CREATED).entity(person).build();
    }

    @GET
    public List<Person> list() {
        return Person.listAll();
    }
}