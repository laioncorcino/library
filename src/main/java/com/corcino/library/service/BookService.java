package com.corcino.library.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import lombok.AllArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
@Setter
public class BookService {

    private ModelMapper modelMapper;
    private BookRepository bookRepository;

    public BookResponse createBook(BookRequest bookRequest) {
        Book book = modelMapper.map(bookRequest, Book.class);
        log.info("Saving book");
        Book bookSaved = bookRepository.save(book);
        return modelMapper.map(bookSaved, BookResponse.class);
    }

}
