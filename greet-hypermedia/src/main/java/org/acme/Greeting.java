package org.acme;

import io.quarkus.resteasy.reactive.links.RestLinkId;

public class Greeting {

    @RestLinkId
    public String name;

    public String message;

    public Greeting(int id) {
    }

    public Greeting(String name, String message) {
        this.name = name;
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
