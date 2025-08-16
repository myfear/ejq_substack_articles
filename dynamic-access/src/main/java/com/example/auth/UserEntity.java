package com.example.auth;

import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_users")
public class UserEntity extends PanacheEntity {

    @Column(unique = true, nullable = false)
    public String username;

    // Not used for auth in this demo (we use properties-file). Keep for
    // completeness.
    public String password;

    // Optional static role for coarse-grained checks
    public String role;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_groups", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "group_id"))
    public Set<GroupEntity> groups;
}