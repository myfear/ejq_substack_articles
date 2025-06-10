package org.acme;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

@Path("/meme")
public class MemeResource {

    @Inject
    MemeService memeService;

    @GET
    @Produces("image/jpeg")
    public Response createMeme(@QueryParam("top") String top,
                               @QueryParam("bottom") String bottom) {
        byte[] meme = memeService.generateMeme(top, bottom);
        return Response.ok(meme).build();
    }
}