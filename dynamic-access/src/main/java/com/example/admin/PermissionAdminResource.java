package com.example.admin;

import com.example.admin.PermissionAdminService.PermissionRequest;
import com.example.admin.PermissionAdminService.PermissionResult;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/admin/permissions")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class PermissionAdminResource {

    @Inject
    PermissionAdminService permissionService;

    @POST
    public Response grant(PermissionRequest req) {
        PermissionResult result = permissionService.grantPermission(req);
        
        if (result.wasCreated) {
            return Response.status(Response.Status.CREATED).entity(result.permission).build();
        } else {
            return Response.ok(result.permission).build();
        }
    }

    @DELETE
    @Path("/{id}")
    public Response revoke(@PathParam("id") Long id) {
        if (!permissionService.revokePermission(id)) {
            throw new NotFoundException("permission not found: " + id);
        }
        return Response.noContent().build();
    }
}