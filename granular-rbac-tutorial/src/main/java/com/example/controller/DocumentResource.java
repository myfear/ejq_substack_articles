package com.example.controller;

import org.eclipse.microprofile.jwt.JsonWebToken;

import com.example.dto.DocumentDto;
import com.example.dto.PermissionDto;
import com.example.entity.Document;
import com.example.entity.User;
import com.example.service.PermissionService;

import io.quarkus.security.Authenticated;
import io.quarkus.security.PermissionsAllowed;
import io.quarkus.security.identity.SecurityIdentity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/documents")
@Authenticated
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DocumentResource {

    @Inject
    SecurityIdentity identity;
    @Inject
    PermissionService perms;

    private User currentUser() {
        JsonWebToken jwt = (JsonWebToken) identity.getPrincipal();
        String userId = jwt.getClaim("user_id");
        Long id = Long.parseLong(userId);
        return User.findById(id);
    }

    @POST
    @Transactional
    public Response create(DocumentDto in) {
        User me = currentUser();
        Document d = new Document();
        d.title = in.title;
        d.content = in.content;
        d.owner = me;
        d.persistAndFlush();
        return Response.status(Response.Status.CREATED).entity(toDto(d)).build();
    }

    @GET
    @Path("/{id}")
    @PermissionsAllowed("document-read")
    public DocumentDto get(@PathParam("id") Long id) {
        Document d = Document.findById(id);
        if (d == null)
            throw new NotFoundException();
        return toDto(d);
    }

    @PUT
    @Path("/{id}")
    @PermissionsAllowed("document-write")
    @Transactional
    public DocumentDto update(@PathParam("id") Long id, DocumentDto in) {
        Document d = Document.findById(id);
        if (d == null)
            throw new NotFoundException();
        d.title = in.title;
        d.content = in.content;
        return toDto(d);
    }

    @DELETE
    @Path("/{id}")
    @PermissionsAllowed("document-delete")
    @Transactional
    public Response delete(@PathParam("id") Long id) {
        if (!Document.deleteById(id))
            throw new NotFoundException();
        return Response.noContent().build();
    }

    // --- ACL endpoints ----------------------------------------------------

    @POST
    @Path("/{id}/permissions")
    @PermissionsAllowed("document-share")
    @Transactional
    public Response grant(@PathParam("id") Long id, PermissionDto dto) {

        User me = currentUser();
        Document doc = Document.findById(id);
        if (doc == null)
            throw new NotFoundException();

        User target = User.findByUsername(dto.targetUsername);
        if (target == null)
            throw new NotFoundException("User not found");

        perms.grantPermission(target, doc, dto.right, me);
        return Response.ok("granted").build();
    }

    @DELETE
    @Path("/{id}/permissions")
    @PermissionsAllowed("document-share")
    @Transactional
    public Response revoke(@PathParam("id") Long id, PermissionDto dto) {

        User me = currentUser();
        Document doc = Document.findById(id);
        if (doc == null)
            throw new NotFoundException();

        User target = User.findByUsername(dto.targetUsername);
        if (target == null)
            throw new NotFoundException("User not found");

        perms.revokePermission(target, doc, dto.right, me);
        return Response.ok("revoked").build();
    }

    private static DocumentDto toDto(Document d) {
        DocumentDto out = new DocumentDto();
        out.id = d.id;
        out.title = d.title;
        out.content = d.content;
        out.ownerUsername = d.owner.username;
        out.createdAt = d.createdAt;
        out.updatedAt = d.updatedAt;
        return out;
    }
}