package org.acme;

import org.kie.api.runtime.KieRuntimeBuilder;
import org.kie.api.runtime.KieSession;

import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/approvals")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ApprovalResource {

    @Inject
    KieRuntimeBuilder runtimeBuilder;

    @POST
    @Path("/calculate")
    public ExpenseReport calculateApproval(ExpenseReport report) {
        KieSession ksession = runtimeBuilder.newKieSession();
        ksession.insert(report);
        ksession.fireAllRules();
        ksession.dispose();
        return report;
    }
}