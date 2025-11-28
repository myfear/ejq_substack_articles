package com.festive.wishlist;

import java.util.List;

import org.hibernate.annotations.SoftDelete;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "wishes")
@SoftDelete(columnName = "archived") // holiday archive sack
public class Wish extends PanacheEntity {

    public String description;
    public String priority; // "high", "medium", "low"
    public boolean kidApproved;

    @ManyToOne
    @JoinColumn(name = "wishlist_id")
    public Wishlist wishlist;

    public void wishlist(Wishlist wishlist) {
        this.wishlist = wishlist;
    }

    // Active Record Pattern: find active wishes
    public static List<Wish> findActive() {
        return listAll();
    }

    // Active Record Pattern: find archived wishes (bypasses soft delete filter)
    public static List<Wish> findArchived() {
        // Use native query to bypass soft delete filter and get archived wishes
        @SuppressWarnings("unchecked")
        List<Wish> archived = getEntityManager()
                .createNativeQuery("SELECT * FROM wishes WHERE archived = true", Wish.class)
                .getResultList();
        return archived;
    }

    // Active Record Pattern: restore soft-deleted wish
    public static void restore(Long wishId) {
        getEntityManager().createNativeQuery("UPDATE wishes SET archived = false WHERE id = :id")
                .setParameter("id", wishId)
                .executeUpdate();
    }

    // Active Record Pattern: hard delete (bypasses soft delete)
    public static void hardDelete(Long wishId) {
        getEntityManager().createNativeQuery("DELETE FROM wishes WHERE id = :id")
                .setParameter("id", wishId)
                .executeUpdate();
    }

}