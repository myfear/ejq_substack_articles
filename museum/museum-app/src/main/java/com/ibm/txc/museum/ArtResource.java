package com.ibm.txc.museum;

import java.util.List;

import com.ibm.txc.museum.domain.Art;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/api/art")
@Produces(MediaType.APPLICATION_JSON)
public class ArtResource {
    @GET
    public List<Art> list() {
        return Art.listAll();
    }
}
