package com.acme.totp.dto;

public class TotpVerifyRequest {
    public String username;
    public String code; // 6 digits as string
}
