package com.corcino.library.unit.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import com.corcino.library.service.BookService;
import com.corcino.library.util.BookCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
public class BookServiceTest {

    private ModelMapper mapper;

    @InjectMocks
    private BookService bookService;

    @Mock
    private BookRepository bookRepository;

    @BeforeEach
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

        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Effective Java");
    }

    @Test
    public void should_not_save_a_book_with_duplicated_isbn() {
        Mockito.when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        BookRequest bookRequest = BookCreator.createBookJavaToBeSaved();
        Throwable exception = catchThrowable(() -> bookService.createBook(bookRequest));

        assertThat(exception)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Isbn already used");

        Mockito.verify(bookRepository, Mockito.never()).save(mapper.map(bookRequest, Book.class));
    }

    @Test
    public void should_not_save_a_book_with_duplicated_title() {
        Mockito.when(bookRepository.existsByTitle(anyString())).thenReturn(true);

        BookRequest bookRequest = BookCreator.createBookJavaToBeSaved();
        Throwable exception = catchThrowable(() -> bookService.createBook(bookRequest));

        assertThat(exception)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Title already used");

        Mockito.verify(bookRepository, Mockito.never()).save(mapper.map(bookRequest, Book.class));
    }

}






























