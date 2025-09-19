package com.example.poll;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "poll_vote")
public class Vote extends PanacheEntity {

    public String participant;

    @ManyToOne
    @JsonIgnore
    public TimeSlot timeSlot;
}