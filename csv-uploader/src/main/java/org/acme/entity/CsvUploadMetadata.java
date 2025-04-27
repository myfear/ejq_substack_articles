package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import java.time.LocalDateTime;

@Entity
public class CsvUploadMetadata extends PanacheEntity {
    public String tableName;
    public LocalDateTime uploadTime;
    public int recordCount;
}
