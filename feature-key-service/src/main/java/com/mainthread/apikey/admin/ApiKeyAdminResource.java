package com.mainthread.apikey.admin;

import com.mainthread.apikey.admin.dto.CreateKeyRequest;
import com.mainthread.apikey.admin.dto.CreateKeyResponse;
import com.mainthread.apikey.admin.dto.RotateKeyResponse;

import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/admin/api-keys")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@RolesAllowed("admin")
public class ApiKeyAdminResource {

    private final ApiKeyAdminService service;

    public ApiKeyAdminResource(ApiKeyAdminService service) {
        this.service = service;
    }

    @POST
    public CreateKeyResponse create(@Valid CreateKeyRequest request) {
        return service.create(request);
    }

    @POST
    @Path("/{keyId}/revoke")
    public void revoke(@PathParam("keyId") String keyId) {
        service.revoke(keyId);
    }

    @POST
    @Path("/{keyId}/rotate")
    public RotateKeyResponse rotate(@PathParam("keyId") String keyId) {
        return service.rotate(keyId);
    }
}