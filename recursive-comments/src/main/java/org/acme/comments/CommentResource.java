package org.acme.comments;

import java.util.List;

import org.acme.comments.CommentResourceDtos.CommentResponse;
import org.acme.comments.CommentResourceDtos.NewCommentRequest;
import org.acme.comments.CommentResourceDtos.ThreadNodeResponse;

import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CommentResource {

    @Inject
    CommentService service;

    @Transactional
    @POST
    public Response addRootComment(NewCommentRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new BadRequestException("content must not be empty");
        }
        Comment created = service.addRootComment(request.content());
        CommentResponse dto = toCommentResponse(created);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @Transactional
    @POST
    @Path("/{parentId}/replies")
    public Response addReply(@PathParam("parentId") Long parentId,
            NewCommentRequest request) {
        if (request == null || request.content() == null || request.content().isBlank()) {
            throw new BadRequestException("content must not be empty");
        }
        Comment created = service.addReply(parentId, request.content());
        CommentResponse dto = toCommentResponse(created);
        return Response.status(Response.Status.CREATED).entity(dto).build();
    }

    @GET
    @Path("/thread/{rootId}")
    public ThreadNodeResponse getThread(@PathParam("rootId") Long rootId) {
        CommentThreadNode root = service.getThread(rootId);
        return toThreadNodeResponse(root);
    }

    private static CommentResponse toCommentResponse(Comment c) {
        return new CommentResponse(
                c.getId(),
                c.getParentId(),
                c.getThreadRootId(),
                c.getContent(),
                c.getCreatedAt());
    }

    private static ThreadNodeResponse toThreadNodeResponse(CommentThreadNode node) {
        List<ThreadNodeResponse> replies = node.replies().stream()
                .map(CommentResource::toThreadNodeResponse)
                .toList();

        return new ThreadNodeResponse(
                node.id(),
                node.parentId(),
                node.level(),
                node.content(),
                replies);
    }
}