package org.acme.model;

import java.util.List;

public record BookDto(
        String isbn,
        List<DiscountCoupon> coupons) {
    public Book toEntity() {
        final var book = new Book();
        book.setIsbn(isbn);
        book.setCoupons(coupons);
        return book;
    }
}