package com.example;

import java.time.OffsetDateTime;

import org.hibernate.annotations.TimeZoneColumn;
import org.hibernate.annotations.TimeZoneStorage;
import org.hibernate.annotations.TimeZoneStorageType;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Event extends PanacheEntity {

    public String title;

    @TimeZoneStorage(TimeZoneStorageType.COLUMN)
    @TimeZoneColumn(name = "start_time_offset")
    @Column(name = "start_time", columnDefinition = "timestamp without time zone")
    public OffsetDateTime startTime;

    public String description;
}