package org.acme;

public class Item {
    public String id;
    public String name;
    public String description;

    public Item() {} // Jackson needs this

    public Item(String id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }
}   
