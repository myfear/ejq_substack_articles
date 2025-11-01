package org.acme.plugin;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;

@Path("/plugin")
public class PluginResource {

    @Inject
    PluginService service;

    @GET
    public String run(
            @QueryParam("name") String name,
            @QueryParam("input") String input) {
        Plugin p = service.byName(name != null ? name : "logging");
        String value = input != null ? input : "hello";
        return "[" + p.name() + "] -> " + p.apply(value);
    }
}