package com.ibm.txc.museum.domain;

import java.time.OffsetDateTime;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class SentimentVote extends PanacheEntity {
    @ManyToOne(optional = false)
    public Art art;
    @Column(nullable = false)
    public String label;
    @Column(nullable = false)
    public OffsetDateTime createdAt;
    @Column(nullable = false)
    public String ip;
}