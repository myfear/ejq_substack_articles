package com.example.domain;

import java.util.List;

import com.example.security.AppUser;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity
public class SecretSantaGroup extends PanacheEntity {

    @Column(nullable = false)
    public String name;

    @Column(nullable = false, unique = true)
    public String inviteCode;

    @ManyToOne(optional = false)
    public AppUser owner;

    @OneToMany(mappedBy = "group")
    public List<GroupMembership> members;
}