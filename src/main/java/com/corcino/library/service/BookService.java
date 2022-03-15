package com.corcino.library.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;
import com.corcino.library.error.exception.ObjectNotFoundException;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
@AllArgsConstructor
@Setter
public class BookService {

    private ModelMapper modelMapper;
    private BookRepository bookRepository;

    public Page<BookResponse> listBooks(String author, Pageable pageable) {
        Page<Book> books;

        if (StringUtils.isNotBlank(author)) {
            books = bookRepository.findByAuthorContaining(author, pageable);
        } else {
            books = bookRepository.findAll(pageable);
        }

        return new BookResponse().convertList(books);
    }

    public BookResponse createBook(BookRequest bookRequest) throws Exception {
        Book book = modelMapper.map(bookRequest, Book.class);
        validateDataIntegrityOf(book);

        log.info("Saving book");
        Book bookSaved = saveBook(book);
        return modelMapper.map(bookSaved, BookResponse.class);
    }

    private Book saveBook(Book book) throws Exception {
        try {
            return bookRepository.save(book);
        }
        catch (Exception e) {
            throw new Exception("Error saving book");
        }
    }

    private void validateDataIntegrityOf(Book book) {
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new DataIntegrityViolationException("Isbn already used");
        }
        if (bookRepository.existsByTitle(book.getTitle())) {
            throw new DataIntegrityViolationException("Title already used");
        }
    }

    public BookResponse getBookById(Long bookId) {
        Book book = getBook(bookId);
        return modelMapper.map(book, BookResponse.class);
    }

    private Book getBook(Long bookId) {
        log.info("Buscando book de id {}", bookId );
        Optional<Book> book = bookRepository.findById(bookId);

        return book.orElseThrow(() -> {
            log.error("Livro de id {} nao encontrado", bookId);
            return new ObjectNotFoundException("Book not found");
        });
    }

    public BookResponse updateBook(UpdateBookRequest updateBook, Long bookId) throws Exception {
        Book book = getBook(bookId);

        if (StringUtils.isNotBlank(updateBook.getTitle()) && bookRepository.existsByTitle(updateBook.getTitle())) {
            throw new DataIntegrityViolationException("Title already used");
        } else if (StringUtils.isNotBlank(updateBook.getTitle())) {
            book.setTitle(updateBook.getTitle());
        }

        if (StringUtils.isNotBlank(updateBook.getIsbn()) && bookRepository.existsByIsbn(updateBook.getIsbn())) {
            throw new DataIntegrityViolationException("Isbn already used");
        } else if (StringUtils.isNotBlank(updateBook.getIsbn())) {
            book.setIsbn(updateBook.getIsbn());
        }

        if (StringUtils.isNotBlank(updateBook.getAuthor())) {
            book.setAuthor(updateBook.getAuthor());
        }

        log.info("Atualizando livro " + book.getBookId());
        Book bookUpdated = saveBook(book);

        return modelMapper.map(bookUpdated, BookResponse.class);
    }

    public void deleteBook(Long bookId) {
        Book book = getBook(bookId);
        log.info("Deletando livro {}", book.getTitle());
        bookRepository.deleteById(bookId);
    }

}
