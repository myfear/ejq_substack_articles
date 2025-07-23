package com.example;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditRecord {
    public Instant timestamp;
    public String principal;
    public String clientIp;
    public String httpMethod;
    public String resourcePath;
    public int httpStatus;
}