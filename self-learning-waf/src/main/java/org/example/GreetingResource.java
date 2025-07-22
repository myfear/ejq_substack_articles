package org.example;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/hello")
public class GreetingResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello(@QueryParam("name") String name) {
        return "Hello from Quarkus REST " + name;
    }

    @GET
    @Path("/{param: .+}")
    @Produces(MediaType.TEXT_PLAIN)
    public String helloWithPath(@PathParam("param") String param) {
        return "Hello from Quarkus REST " + param;
    }
}
