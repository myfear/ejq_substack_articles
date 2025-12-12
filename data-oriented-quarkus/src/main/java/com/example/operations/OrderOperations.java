package com.example.operations;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.example.api.OrderRequest;
import com.example.api.OrderResult;
import com.example.api.OrderResult.OutOfStock;
import com.example.api.OrderResult.ProductNotFound;
import com.example.api.OrderResult.Success;
import com.example.api.OrderView;
import com.example.domain.Order;
import com.example.domain.Product;

public final class OrderOperations {

    private OrderOperations() {
    } // Utility class

    /**
     * Pure business logic - transforms request into result
     */
    public static OrderResult placeOrder(OrderRequest request) {
        Product product = Product.find("name", request.productName()).firstResult();

        if (product == null) {
            return new ProductNotFound(request.productName());
        }

        return switch (compareStock(product.stockQuantity, request.quantity())) {
            case SUFFICIENT -> processOrder(request, product);
            case INSUFFICIENT -> new OutOfStock(
                    request.productName(),
                    product.stockQuantity,
                    request.quantity());
        };
    }

    private static OrderResult processOrder(OrderRequest request, Product product) {
        BigDecimal total = product.price.multiply(BigDecimal.valueOf(request.quantity()));

        product.stockQuantity -= request.quantity();
        product.persist();

        Order order = new Order();
        order.customerEmail = request.customerEmail();
        order.productName = request.productName();
        order.quantity = request.quantity();
        order.totalAmount = total;
        order.status = "CONFIRMED";
        order.createdAt = Instant.now();
        order.persist();

        return new Success(
                order.id,
                order.customerEmail,
                order.productName,
                order.quantity,
                order.totalAmount);
    }

    private enum StockLevel {
        SUFFICIENT, INSUFFICIENT
    }

    private static StockLevel compareStock(int available, int requested) {
        return available >= requested ? StockLevel.SUFFICIENT : StockLevel.INSUFFICIENT;
    }

    public static List<OrderView> getAllOrders() {
        return Order.<Order>listAll()
                .stream()
                .map(OrderOperations::toView)
                .toList();
    }

    public static OrderView toView(Order order) {
        return new OrderView(
                order.id,
                order.customerEmail,
                order.productName,
                order.quantity,
                order.totalAmount,
                order.status,
                order.createdAt.toString());
    }
}