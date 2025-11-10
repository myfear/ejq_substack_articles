package com.example;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/cache")
@Produces(MediaType.TEXT_PLAIN)
public class CacheResource {

    @Inject
    ContentCache cache;

    @GET
    public String get(@QueryParam("name") String name, @QueryParam("version") String version) {
        String val = cache.get("page", name, version);
        if (val == null) {
            val = "Generated for " + name + "@" + version;
            cache.put("page", val, name, version);
            return "New entry: " + val;
        }
        return "Cache hit: " + val;
    }
}