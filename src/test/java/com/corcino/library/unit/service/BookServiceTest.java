package com.corcino.library.unit.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import com.corcino.library.service.BookService;
import com.corcino.library.util.BookCreator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(SpringRunner.class)
public class BookServiceTest {

    private ModelMapper mapper;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

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
    public void should_save_book() throws Exception {
        Mockito.when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        Mockito.when(bookRepository.existsByTitle(anyString())).thenReturn(false);

        Mockito.when(bookRepository.save(any(Book.class))).thenReturn(mapper.map(BookCreator.createBookJavaPersisted(), Book.class));

        BookRequest bookRequest = BookCreator.createBookJavaToBeSaved();
        BookResponse response = bookService.createBook(bookRequest);

        assertNotNull(response);
        assertNotNull(response.getBookId());
        assertEquals("Effective Java", response.getTitle());
    }

    @Test
    public void should_not_save_a_book_with_duplicated_isbn() throws Exception {
        expectedException.expect(DataIntegrityViolationException.class);
        expectedException.expectMessage("Isbn already used");

        Mockito.when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        BookRequest bookRequest = BookCreator.createBookJavaToBeSaved();
        bookService.createBook(bookRequest);
    }

    @Test
    public void should_not_save_a_book_with_duplicated_title() throws Exception {
        expectedException.expect(DataIntegrityViolationException.class);
        expectedException.expectMessage("Title already used");

        Mockito.when(bookRepository.existsByTitle(anyString())).thenReturn(true);

        BookRequest bookRequest = BookCreator.createBookJavaToBeSaved();
        bookService.createBook(bookRequest);
    }

}






























