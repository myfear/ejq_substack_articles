package org.acme.repository;

import java.util.List;

import org.acme.model.Book;

import jakarta.data.repository.Find;
import jakarta.data.repository.Insert;
import jakarta.data.repository.Repository;
import jakarta.data.repository.Update;

@Repository
public interface BookRepository {
    @Find
    List<Book> getBooks();

    @Find
    Book findBookById(long id);

    @Find
    Book findBookByIsbn(String isbn);

    @Insert
    Book createBook(Book book);

    @Update
    void updateBook(Book book);
}
