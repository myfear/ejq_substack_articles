package org.acme.hibernate.search.elasticsearch.model;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

import org.hibernate.search.engine.backend.types.Sortable;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;

import com.fasterxml.jackson.annotation.JsonIgnore; // To break cycles during serialization
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Indexed // This "item of interest" is also indexed
public class Book extends PanacheEntity {

    @FullTextField(analyzer = "english") // Titles searched using the "english" analyzer
    @KeywordField(name = "title_sort", sortable = Sortable.YES, normalizer = "sort")
    public String title;

    @ManyToOne
    @JsonIgnore // Avoids a loop when serializing Author <-> Book
    public Author author;

    public Book() {
    }
    // Consider adding toString, equals, and hashCode
}