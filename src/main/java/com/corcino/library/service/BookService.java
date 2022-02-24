package com.corcino.library.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
@Setter
public class BookService {

    private ModelMapper modelMapper;
    private BookRepository bookRepository;

    public BookResponse createBook(BookRequest bookRequest) throws Exception {
        Book book = modelMapper.map(bookRequest, Book.class);
        log.info("Saving book");
        Book bookSaved = saveBook(book);
        return modelMapper.map(bookSaved, BookResponse.class);
    }

    private Book saveBook(Book book) throws Exception {
        validateDataIntegrityOf(book);
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

}
