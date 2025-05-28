package org.acme.hibernate.search.elasticsearch.model;

import java.util.List;
import java.util.ArrayList; // Added for initialization
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.FullTextField;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.Indexed;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.IndexedEmbedded;
import org.hibernate.search.mapper.pojo.mapping.definition.annotation.KeywordField;
import org.hibernate.search.engine.backend.types.Sortable; // For Sortable.YES
import io.quarkus.hibernate.orm.panache.PanacheEntity;

@Entity
@Indexed // This "suspect" is now under surveillance (indexed)
public class Author extends PanacheEntity {

    @FullTextField(analyzer = "name") // Analyzed for full-text search using the "name" analyzer
    @KeywordField(name = "firstName_sort", sortable = Sortable.YES, normalizer = "sort") // For exact matches and
                                                                                         // sorting
    public String firstName;

    @FullTextField(analyzer = "name")
    @KeywordField(name = "lastName_sort", sortable = Sortable.YES, normalizer = "sort")
    public String lastName;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @IndexedEmbedded // Their "associates" (books) are also part of the profile
    public List<Book> books = new ArrayList<>(); // Initialize to avoid NullPointerExceptions

    public Author() {
    }

    // Consider adding toString, equals, and hashCode if not relying solely on
    // PanacheEntity's
}