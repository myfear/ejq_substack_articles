package com.ibm.txc.museum.shop.domain;

import java.math.BigDecimal;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

@Entity
@Table(name = "shop_item", indexes = {
    @Index(name = "idx_item_artwork", columnList = "artworkName"),
    @Index(name = "idx_item_sku", columnList = "sku", unique = true)
})
public class ShopItem extends PanacheEntity {

    @Column(nullable = false, unique = true)
    public String sku;

    @Column(nullable = false)
    public String title;

    @Column(nullable = false)
    public String artworkName;

    @Column(nullable = false, precision = 12, scale = 2)
    public BigDecimal price;

    @Column(nullable = false)
    public int stock;

    @Column(length = 2000)
    public String description;
}
