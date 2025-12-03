package com.ibm.developer.example;

public record Project(
        long id,
        String name,
        String owner,
        String status) {
}