package com.acme.totp.dto;

public class RegisterResponse {
    public String otpauthUri; // e.g. otpauth://totp/Issuer:alice?secret=...&issuer=Issuer...
    public String qrUrl; // http link to PNG in this app
    public String note; // loud warning that this is a demo
}
