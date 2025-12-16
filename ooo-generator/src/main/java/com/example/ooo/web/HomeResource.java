package com.example.ooo.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HomeResource {

    @Inject
    Template home;

    @GET
    public TemplateInstance page() {
        return home.instance();
    }
}