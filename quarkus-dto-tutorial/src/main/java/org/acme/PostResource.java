package org.acme;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.acme.domain.Post;
import org.acme.dto.*;
import org.acme.mapper.PostMapper; // Using MapStruct
// import org.acme.mapper.ManualPostMapper; // Or using manual mapper

import java.util.List;

@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

    @Inject
    PostMapper mapper;

    @POST
    @Transactional
    public Response create(CreatePostDto dto) {
        Post post = mapper.toEntity(dto);
        post.persist();
        return Response.status(Response.Status.CREATED).entity(mapper.toDto(post)).build();
    }

    @GET
    public List<PostDto> list() {
        return mapper.toDtoList(Post.listAll());
    }

    @GET
    @Path("/{id}")
    public Response get(@PathParam("id") Long id) {
        Post post = Post.findById(id);
        if (post == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        return Response.ok(mapper.toDto(post)).build();
    }

    @PUT
    @Path("/{id}")
    @Transactional
    public Response update(@PathParam("id") Long id, UpdatePostDto dto) {
        Post post = Post.findById(id);
        if (post == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        mapper.updateEntityFromDto(dto, post);
        return Response.ok(mapper.toDto(post)).build();
    }

    @DELETE
    @Path("/{id}")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        Post post = Post.findById(id);
        if (post == null)
            return Response.status(Response.Status.NOT_FOUND).build();
        post.delete();
        return Response.noContent().build();
    }
}
