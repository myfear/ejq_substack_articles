package org.acme.errorhandling;

public class MyCustomApplicationException extends RuntimeException {
    public MyCustomApplicationException(String message) {
        super(message);
    }
}
