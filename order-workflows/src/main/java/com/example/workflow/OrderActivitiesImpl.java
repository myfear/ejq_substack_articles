package com.example.workflow;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class OrderActivitiesImpl implements OrderActivities {

    @Override
    public void reserveInventory(String orderId) {
        Log.infof("Inventory reserved for %s", orderId);
    }

    @Override
    public void chargePayment(String orderId) {
        Log.infof("Payment charged for %s", orderId);
    }

    @Override
    public void confirmOrder(String orderId) {
        Log.infof("Order confirmed %s", orderId);
    }

    @Override
    public void releaseInventory(String orderId) {
        Log.infof("Inventory released for %s", orderId);
    }

    @Override
    public void refundPayment(String orderId) {
        Log.infof("Payment refunded for %s", orderId);
    }
}