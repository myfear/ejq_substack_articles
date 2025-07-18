package com.example;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProblemDetail {
    public String type; // URI reference that identifies the problem type
    public String title; // Short, human-readable summary of the problem
    public int status; // HTTP status code
    public String detail; // Human-readable explanation
    public String instance; // Optional URI reference that identifies the request instance
}