package org.acme;

public class ErrorResponse {
    public String errorCode;
    public String message;
    public long timestamp;

    public ErrorResponse() {}

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.timestamp = System.currentTimeMillis();
    }
}
