package com.example.gists;

import java.time.Instant;

import com.example.gists.model.CreateGistRequest;
import com.example.gists.model.Gist;
import com.example.gists.store.S3GistStore;

import de.huxhorn.sulky.ulid.ULID;
import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class GistResource {

    private static final ULID ULID = new ULID();

    @Inject
    S3GistStore store;
    @Inject
    MarkdownService markdown;

    @Location("gist.html")
    Template gistTemplate;

    @POST
    @Path("/gists")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(CreateGistRequest req) {
        if (req == null || req.markdown == null || req.markdown.isBlank())
            throw new BadRequestException("Markdown required");

        Gist gist = new Gist();
        gist.id = ULID.nextULID().toLowerCase();
        gist.title = req.title;
        gist.language = req.language;
        gist.markdown = req.markdown;
        gist.html = markdown.toSafeHtml(req.markdown);
        gist.createdAt = Instant.now();

        store.save(gist);
        return Response.ok(gist).build();
    }

    @GET
    @Path("/gists/{id}.json")
    public Gist get(@PathParam("id") String id) {
        Gist gist = store.find(id);
        if (gist == null)
            throw new NotFoundException();
        return gist;
    }

    @GET
    @Path("/g/{id}")
    @Produces(MediaType.TEXT_HTML)
    public String page(@PathParam("id") String id) {
        Gist gist = store.find(id);
        if (gist == null)
            throw new NotFoundException();
        return gistTemplate.data("gist", gist).render();
    }
}