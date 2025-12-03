package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class TenantResource {

    @Inject
    TenantContext tenantContext;

    @Inject
    TenantDataService dataService;

    @GET
    @Path("/data")
    public Response getData() {
        String tenant = tenantContext.getTenantId();
        List<String> data = dataService.getData(tenant);

        Map<String, Object> response = new HashMap<>();
        response.put("tenant", tenant);
        response.put("data", data);
        response.put("timestamp", System.currentTimeMillis());

        return Response.ok(response).build();
    }

    @POST
    @Path("/data")
    public Response addData(Map<String, String> payload) {
        String tenant = tenantContext.getTenantId();
        String item = payload.get("item");

        if (item == null || item.isBlank()) {
            return Response.status(400)
                    .entity(Map.of("error", "Item cannot be empty"))
                    .build();
        }

        dataService.addData(tenant, item);

        return Response.ok(Map.of(
                "tenant", tenant,
                "message", "Item added successfully",
                "item", item)).build();
    }

    @GET
    @Path("/info")
    public Response getTenantInfo() {
        String tenant = tenantContext.getTenantId();

        return Response.ok(Map.of(
                "tenant", tenant,
                "message", "Welcome to " + tenant + "'s space!",
                "server", "Quarkus + nip.io Multi-Tenant API")).build();
    }
}