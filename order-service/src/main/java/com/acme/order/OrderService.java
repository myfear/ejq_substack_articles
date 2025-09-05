package com.acme.order;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import io.quarkiverse.businessscore.BusinessScore;
import io.quarkiverse.businessscore.Scored;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class OrderService {

    private final Map<String, Order> db = new ConcurrentHashMap<>();

    @Inject
    BusinessScore businessScore;

    public Order create(String item, int qty) {
        String id = UUID.randomUUID().toString();
        Order o = new Order(id, item, qty, "CREATED");
        db.put(id, o);
        return o;
    }

    public Order get(String id) {
        return db.get(id);
    }

    /**
     * Completing an order is the business moment that matters.
     * 
     * @Scored ensures Business Score increments when this method succeeds.
     */
    @Scored(5)
    public Order complete(String id) {
        Order o = db.get(id);
        if (o == null)
            return null;
        Order completed = new Order(o.id(), o.item(), o.qty(), "COMPLETED");
        db.put(id, completed);
        return completed;
    }

    /**
     * Example of scoring programmatically, in case the increment depends on logic.
     */
    public void scoreManually(int value) {
        businessScore.score(value);
    }
}