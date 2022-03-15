package com.corcino.library.repository;

import com.corcino.library.model.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);
    boolean existsByTitle(String title);

    Page<Book> findByAuthorContaining(String author, Pageable pageable);
}
