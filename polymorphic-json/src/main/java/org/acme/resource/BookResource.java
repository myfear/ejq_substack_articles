package org.acme.resource;

import java.util.Collections;
import java.util.List;

import org.acme.model.Book;
import org.acme.model.BookDto;
import org.acme.repository.BookRepository;
import org.hibernate.Session;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;

@Path("/books")
public class BookResource {

    @Inject
    BookRepository bookRepository;
    @Inject
    Session session;

    @GET
    public List<Book> getBooks(@QueryParam("isbn") String isbn) {
        return isbn == null
                ? bookRepository.getBooks()
                : Collections.singletonList(bookRepository.findBookByIsbn(isbn));
    }

    @GET
    @Path("{id}")
    public Book getBook(@PathParam("id") long id) {
        return bookRepository.findBookById(id);
    }

    @PUT
    @Path("{id}")
    public void updateBook(@PathParam("id") long id, BookDto dto) {
        final var book = dto.toEntity();
        book.setId(id);
        bookRepository.updateBook(book);
    }

    @POST
    public Book addBook(BookDto dto) {
        return bookRepository.createBook(dto.toEntity());
    }
}
