package com.example;

import jdk.jfr.Category;
import jdk.jfr.Event;
import jdk.jfr.Label;
import jdk.jfr.Name;

@Name("com.example.BusinessOperation")
@Label("Business Operation")
@Category("Application")
public class BusinessEvent extends Event {
    @Label("Operation")
    String operation;
    @Label("User ID")
    String userId;
    @Label("Items Processed")
    int items;

    public BusinessEvent(String op, String user, int items) {
        this.operation = op;
        this.userId = user;
        this.items = items;
    }

    public static void record(String op, String user, int items) {
        BusinessEvent e = new BusinessEvent(op, user, items);
        e.commit();
    }
}