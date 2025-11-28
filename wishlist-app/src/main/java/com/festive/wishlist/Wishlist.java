package com.festive.wishlist;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.SoftDelete;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishlists")
@SoftDelete // adds deleted BOOLEAN column
public class Wishlist extends PanacheEntity {

    public String ownerName;

    @OneToMany(mappedBy = "wishlist", cascade = CascadeType.ALL, orphanRemoval = true)
    List<Wish> wishes = new ArrayList<>();

    public void addWish(Wish wish) {
        wishes.add(wish);
        wish.wishlist(this);
    }

    // Active Record Pattern: static query methods
    public static List<Wishlist> findActive() {
        return listAll();
    }

}