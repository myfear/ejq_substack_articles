package com.example.domain;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class SecretSantaPairing extends PanacheEntity {

    @ManyToOne(optional = false)
    public SecretSantaGroup group;

    @ManyToOne(optional = false)
    public GroupMembership giver;

    @ManyToOne(optional = false)
    public GroupMembership receiver;
}