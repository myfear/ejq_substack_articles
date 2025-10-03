package com.acme.api;

import java.util.Properties;

import com.acme.domain.Invoice;

import jakarta.batch.operations.JobOperator;
import jakarta.batch.runtime.BatchRuntime;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/batch/invoices")
@Produces(MediaType.APPLICATION_JSON)
public class BatchResource {

    private final JobOperator jobOperator = BatchRuntime.getJobOperator();

    @Inject
    EntityManager em;

    @POST
    @Path("/run")
    public Response run(@QueryParam("year") int year,
            @QueryParam("bundesland") @DefaultValue("") String bundesland,
            @QueryParam("chunk") @DefaultValue("200") int chunk,
            @QueryParam("dryRun") @DefaultValue("false") boolean dryRun) {
        Properties p = new Properties();
        p.setProperty("year", String.valueOf(year));
        p.setProperty("bundesland", bundesland);
        p.setProperty("chunkSize", String.valueOf(chunk));
        p.setProperty("dryRun", String.valueOf(dryRun));

        long execId = jobOperator.start("year-end-invoice", p);
        return Response.accepted().entity(new StartResponse(execId)).build();
    }

    @GET
    @Path("/status/{executionId}")
    public Response status(@PathParam("executionId") long id) {
        var exec = jobOperator.getJobExecution(id);
        var steps = jobOperator.getStepExecutions(id);
        return Response.ok(new StatusResponse(
                new ExecutionInfo(exec.getExecutionId(), exec.getJobName(), exec.getBatchStatus().toString(),
                        exec.getExitStatus(),
                        exec.getStartTime() != null
                                ? exec.getStartTime().toInstant().atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDateTime()
                                : null,
                        exec.getEndTime() != null
                                ? exec.getEndTime().toInstant().atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDateTime()
                                : null),
                steps.stream().map(step -> new StepInfo(step.getStepName(), step.getBatchStatus().toString(),
                        step.getExitStatus(),
                        step.getStartTime() != null
                                ? step.getStartTime().toInstant().atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDateTime()
                                : null,
                        step.getEndTime() != null
                                ? step.getEndTime().toInstant().atZone(java.time.ZoneId.systemDefault())
                                        .toLocalDateTime()
                                : null))
                        .toList()))
                .build();
    }

    @GET
    @Path("/list")
    public Response listInvoices() {
        try {
            var invoices = em.createQuery("SELECT i FROM Invoice i", Invoice.class).getResultList();
            return Response.ok("Found " + invoices.size() + " invoices in database").build();
        } catch (Exception e) {
            return Response.ok("Error querying invoices: " + e.getMessage()).build();
        }
    }

    public record StartResponse(long executionId) {
    }

    public record StatusResponse(ExecutionInfo execution, java.util.List<StepInfo> steps) {
    }

    public record ExecutionInfo(long executionId, String jobName, String batchStatus,
            String exitStatus, java.time.LocalDateTime startTime,
            java.time.LocalDateTime endTime) {
    }

    public record StepInfo(String stepName, String batchStatus, String exitStatus,
            java.time.LocalDateTime startTime, java.time.LocalDateTime endTime) {
    }
}