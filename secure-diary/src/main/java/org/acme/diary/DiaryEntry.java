package org.acme.diary;

import java.time.LocalDate;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class DiaryEntry extends PanacheEntity {

    public LocalDate entryDate;

    @Column(columnDefinition = "TEXT")
    public String encryptedContent;

    public boolean archived = false;
}