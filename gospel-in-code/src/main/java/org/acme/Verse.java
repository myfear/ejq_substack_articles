package org.acme;

import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity
public class Verse extends PanacheEntity {

    // e.g., "KJV", "OEB"
    public String translation;

    // e.g., "John", "Genesis"
    public String book;

    public int chapter;

    public int verseNum;

    @Column(columnDefinition = "text")
    public String text;

    // This annotation tells Hibernate to use the 'vector' data type in Postgres.
    // The size of the vector depends on the embedding model used.
    @Column
    @JdbcTypeCode(SqlTypes.VECTOR)
    @Array(length = 384) // dimensions
    private float[] embedding;

    public void setEmbedding(float[] embedding) {
        this.embedding = embedding;
    }

    public float[] getEmbedding() {
        return embedding;
    }


}