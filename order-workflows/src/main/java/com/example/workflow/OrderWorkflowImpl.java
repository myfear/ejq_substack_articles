package com.example.workflow;

import java.time.Duration;

import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;

public class OrderWorkflowImpl implements OrderWorkflow {

    private final OrderActivities activities = Workflow.newActivityStub(
            OrderActivities.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofSeconds(10))
                    .setRetryOptions(
                            io.temporal.common.RetryOptions.newBuilder()
                                    .setMaximumAttempts(3)
                                    .build())
                    .build());

    @Override
    public void processOrder(String orderId) {

        try {
            activities.reserveInventory(orderId);
            activities.chargePayment(orderId);
            activities.confirmOrder(orderId);

        } catch (Exception e) {

            Workflow.getLogger(this.getClass())
                    .error("Order failed, compensating", e);

            compensate(orderId);
            throw e;
        }
    }

    private void compensate(String orderId) {
        activities.refundPayment(orderId);
        activities.releaseInventory(orderId);
    }
}