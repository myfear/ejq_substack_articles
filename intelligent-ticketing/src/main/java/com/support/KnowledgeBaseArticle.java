package com.support;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class KnowledgeBaseArticle extends PanacheEntity {
    public String title;
    @Column(columnDefinition = "TEXT")
    public String content;

    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384)
    public float[] embedding;
}
