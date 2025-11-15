package com.example.wordcloud.web;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/")
public class HomeResource {

    @Inject
    Template wordcloud_index;

    @GET
    public TemplateInstance index() {
        return wordcloud_index.instance();
    }
}