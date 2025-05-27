package org.acme.hibernate.search.elasticsearch;

import java.util.List;

import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.acme.hibernate.search.elasticsearch.model.Author;
import org.acme.hibernate.search.elasticsearch.model.Book;
import org.hibernate.search.mapper.orm.mapping.SearchMapping;
import org.hibernate.search.mapper.orm.session.SearchSession;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.RestQuery; // For query parameters

import io.quarkus.runtime.StartupEvent;

@Path("/library")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON) // Default for body if not form params
public class LibraryResource {

    @Inject
    SearchSession searchSession;

    // --- Author CRUD ---
    @PUT
    @Path("author")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED) // As per original guide
    public void addAuthor(@RestForm String firstName, @RestForm String lastName) {
        Author author = new Author();
        author.firstName = firstName;
        author.lastName = lastName;
        author.persist();
    }

    @GET
    @Path("author")
    public List<Author> getAllAuthors() {
        return Author.listAll();
    }

    @POST
    @Path("author/{id}")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void updateAuthor(Long id, @RestForm String firstName, @RestForm String lastName) {
        Author author = Author.findById(id);
        if (author != null) {
            author.firstName = firstName;
            author.lastName = lastName;
            author.persist();
        }
    }

    @DELETE
    @Path("author/{id}")
    @Transactional
    public void deleteAuthor(Long id) {
        Author author = Author.findById(id);
        if (author != null) {
            author.delete(); // This will also remove associated books if CascadeType.ALL is set
        }
    }

    // --- Book CRUD ---
    @PUT
    @Path("book")
    @Transactional
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public void addBook(@RestForm String title, @RestForm Long authorId) {
        Author author = Author.findById(authorId);
        if (author == null) {
            // Consider throwing a WebApplicationException (e.g., NotFoundException)
            return;
        }
        Book book = new Book();
        book.title = title;
        book.author = author;
        book.persist(); // Persist the book

        author.books.add(book); // Add book to author's list
        author.persist(); // Update the author to establish the relationship fully
    }

    @GET
    @Path("book")
    public List<Book> getAllBooks() {
        return Book.listAll();
    }

    @DELETE
    @Path("book/{id}")
    @Transactional
    public void deleteBook(Long id) {
        Book book = Book.findById(id);
        if (book != null) {
            if (book.author != null) {
                book.author.books.remove(book); // Maintain consistency
                // book.author.persist(); // Not strictly needed if managed by Hibernate
            }
            book.delete();
        }
    }

    // --- Search Operations ---
    @GET
    @Path("author/search")
    @Transactional
    public List<Author> searchAuthors(@RestQuery String pattern, @RestQuery Integer size) {
        if (size == null)
            size = 10; // Default page size
        return searchSession.search(Author.class)
                .where(f -> pattern == null || pattern.isBlank() ? f.matchAll()
                        : f.simpleQueryString()
                                .fields("firstName", "lastName", "books.title") // Search across author names and book
                                                                                // titles
                                .matching(pattern))
                .fetchHits(size);
    }

    @GET
    @Path("book/search")
    @Transactional
    public List<Book> searchBooks(@RestQuery String pattern, @RestQuery String authorLastName,
            @RestQuery Integer size) {
        if (size == null)
            size = 10;
        return searchSession.search(Book.class)
                .where(f -> {
                    var bool = f.bool();
                    bool.must(pattern == null || pattern.isBlank() ? f.matchAll()
                            : f.simpleQueryString()
                                    .field("title")
                                    .matching(pattern));
                    if (authorLastName != null && !authorLastName.isBlank()) {
                        bool.must(f.match().field("author.lastName_sort").matching(authorLastName)); // Match on keyword
                                                                                                    // field for author's
                                                                                                    // last name
                    }
                    return bool;
                })
                .sort(f -> f.field("title_sort").asc()) // Assuming you add a title_sort KeywordField to Book
                .fetchHits(size);
    }

    @Inject
    SearchMapping searchMapping;

    void onStart(@Observes StartupEvent ev) throws InterruptedException {
        // only reindex if we imported some content
        if (Book.count() > 0) {
            searchMapping.scope(Object.class)
                    .massIndexer()
                    .startAndWait();
        }
    }

}