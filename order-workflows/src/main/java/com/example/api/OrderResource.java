package com.example.api;

import com.example.workflow.OrderWorkflow;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/orders")
public class OrderResource {

    @Inject
    WorkflowClient workflowClient;

    @POST
    @Path("/{id}")
    public void start(String id) {

        OrderWorkflow workflow = workflowClient.newWorkflowStub(
                OrderWorkflow.class,
                WorkflowOptions.newBuilder()
                        .setTaskQueue("OrderWorkflow")
                        .build());

        WorkflowClient.start(workflow::processOrder, id);
    }
}