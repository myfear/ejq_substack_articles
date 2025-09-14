package com.acme.totp.dto;

public class TanVerifyRequest {
    public String username;
    public String canonical; // must match server-provided canonical string
    public String tan; // user-provided TAN code (string)
}
