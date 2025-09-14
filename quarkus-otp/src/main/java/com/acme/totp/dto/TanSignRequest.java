package com.acme.totp.dto;

/** DEMO ONLY: used to simulate a user's device computing the TAN locally. */
public class TanSignRequest {
    public String username; // whose device/secret to use (demo)
    public String canonical; // exact canonical string from /tan/challenge
    public Integer digits; // optional; default 8
}
