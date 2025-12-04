package com.example.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class GroupMembership extends PanacheEntity {

    @ManyToOne(optional = false)
    public SecretSantaGroup group;

    @Column(nullable = false)
    public String participantName;

    @Column(nullable = false)
    public String participantEmail;

    @Column(length = 2000)
    public String wishlist;
}