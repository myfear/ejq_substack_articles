package com.example;

import com.example.api.OrderRequest;
import com.example.api.OrderResult.OutOfStock;
import com.example.operations.OrderOperations;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

@QuarkusTest
class OrderResourceTest {
    @Test
    void testPlaceOrderOutOfStock() {
        var request = new OrderRequest("test@example.com", "Monitor", 10);
        var result = OrderOperations.placeOrder(request);
        assertInstanceOf(OutOfStock.class, result);
    }

}