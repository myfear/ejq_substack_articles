package com.acme.api;

import java.util.List;

import com.acme.model.Customer;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/")
public class DashboardResource {

    @Inject
    Template dashboard;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get() {

        List<Customer> customers = List.of(
                new Customer("C001", "Acme Corp", "contact@acme.com", "Active",
                        "Discussed Q4 contract renewal."),
                new Customer("C002", "Globex", "info@globex.com", "Pending",
                        "Waiting on procurement approval."),
                new Customer("C003", "Soylent Corp", "sales@soylent.com", "Inactive",
                        "Contract expired last month."));

        return dashboard.data("customers", customers);
    }
}