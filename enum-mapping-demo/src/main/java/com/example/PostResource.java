package com.example;

import java.util.List;

import com.example.entity.Post;
import com.example.model.PostStatus;
import com.example.repository.PostRepository;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/posts")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PostResource {

    @Inject
    PostRepository postRepository;

    @POST
    @Transactional
    public Response create(Post post) {
        post.persist();
        return Response.status(Response.Status.CREATED).entity(post).build();
    }

    @GET
    public List<Post> listAll() {
        return Post.listAll();
    }

    @GET
    @Path("/{id}")
    public Post getById(@PathParam("id") Integer id) {
        return postRepository.findByIdOptional(id.longValue())
                .orElseThrow(() -> new WebApplicationException("Post not found", 404));
    }

    @GET
    @Path("/status/{status}")

    public List<Post> findByStatus(@PathParam("status") PostStatus status) {
        return postRepository.findByStatus(status);
    }
}