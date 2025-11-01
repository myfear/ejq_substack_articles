package org.acme.model;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "Book")
@Table(name = "book")
public class Book {

    @Id
    @GeneratedValue
    private Long id;

    @NaturalId
    @Column(length = 15)
    private String isbn;

    @JdbcTypeCode(SqlTypes.JSON_ARRAY)
    private List<DiscountCoupon> coupons = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public List<DiscountCoupon> getCoupons() {
        return coupons;
    }

    public void setCoupons(List<DiscountCoupon> coupons) {
        this.coupons = coupons;
    }
}
