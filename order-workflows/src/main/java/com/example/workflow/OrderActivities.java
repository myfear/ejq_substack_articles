package com.example.workflow;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface OrderActivities {

    @ActivityMethod
    void reserveInventory(String orderId);

    @ActivityMethod
    void chargePayment(String orderId);

    @ActivityMethod
    void confirmOrder(String orderId);

    @ActivityMethod
    void releaseInventory(String orderId);

    @ActivityMethod
    void refundPayment(String orderId);
}