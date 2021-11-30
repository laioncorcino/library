package com.corcino.library.unit.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import com.corcino.library.service.BookService;
import com.corcino.library.util.BookCreator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;

@RunWith(SpringRunner.class)
public class BookServiceTest {

    private ModelMapper mapper;

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @Before
    public void setUp() {
        mapper = new ModelMapper();
        bookService.setModelMapper(mapper);
    }

    @Test
    public void should_save_book() {
        Mockito.when(bookRepository.save(any(Book.class))).thenReturn(mapper.map(BookCreator.createBookJavaPersisted(), Book.class));

        BookRequest bookRequest = BookCreator.createBookJavaToBeSaved();
        BookResponse response = bookService.createBook(bookRequest);

        assertNotNull(response);
        assertNotNull(response.getBookId());
        assertEquals("Effective Java", response.getTitle());
    }

}






























