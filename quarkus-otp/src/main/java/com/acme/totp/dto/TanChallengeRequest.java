package com.acme.totp.dto;

import java.math.BigDecimal;

public class TanChallengeRequest {
    public String username;
    public String txId;
    public BigDecimal amount;
    public String currency;
    public String beneficiary;
}