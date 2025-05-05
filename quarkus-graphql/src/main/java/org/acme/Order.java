package org.acme;

public class Order {

    public Order(String id, double total) {
        this.id = id;
        this.total = total;
    }
    private String id;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public double getTotal() {
        return total;
    }
    public void setTotal(double total) {
        this.total = total;
    }
    private double total;
}

