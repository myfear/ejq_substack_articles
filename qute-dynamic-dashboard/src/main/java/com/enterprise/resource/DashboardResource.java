package com.enterprise.resource;

import com.enterprise.model.DashboardConfig;
import com.enterprise.service.DashboardService;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/dashboard")
public class DashboardResource {

    @Inject
    Template dashboard;

    @Inject
    DashboardService dashboardService;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getDashboard(@QueryParam("user") String username) {
        if (username == null || username.isEmpty()) {
            username = "employee";
        }
        DashboardConfig config = dashboardService.getDashboardConfig(username);
        return dashboard.data("config", config);
    }
}