package com.acme.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HomeResource {

    @Inject
    Template index;

    @GET
    public TemplateInstance home() {
        return index.instance();
    }
}