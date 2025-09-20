package com.example.api;

import java.util.List;

import com.example.StartupService;
import com.example.view.ClusterView;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/discover")
public class DiscoverPage {

    @Inject
    StartupService startupService;

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance page(List<ClusterView> clusters, int totalArticles, int totalKeywords);
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance render() {
        List<ClusterView> clusters = startupService.getClusters();
        
        // Calculate totals
        int totalArticles = clusters.stream().mapToInt(c -> c.articles.size()).sum();
        int totalKeywords = clusters.stream().mapToInt(c -> c.keywords.size()).sum();
        
        return Templates.page(clusters, totalArticles, totalKeywords);
    }
}