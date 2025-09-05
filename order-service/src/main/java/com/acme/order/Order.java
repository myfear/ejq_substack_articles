package com.acme.order;

public record Order(String id, String item, int qty, String status) {
}