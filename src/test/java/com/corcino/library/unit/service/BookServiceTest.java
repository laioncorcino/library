package com.corcino.library.unit.service;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;
import com.corcino.library.error.exception.ObjectNotFoundException;
import com.corcino.library.model.Book;
import com.corcino.library.repository.BookRepository;
import com.corcino.library.service.BookService;
import com.corcino.library.util.BookCreator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;

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
    @DisplayName("should_return_paginated_list_of_books")
    public void listBookPaginated() {
        List<Book> bookList = Arrays.asList(toModel(BookCreator.bookJavaPersisted()), toModel(BookCreator.bookGolangPersisted()));
        PageImpl<Book> bookListPaginated = new PageImpl<>(bookList, buildPageable(), 2);

        Mockito.when(bookRepository.findAll(any(PageRequest.class))).thenReturn(bookListPaginated);
        Page<BookResponse> books = bookService.listBooks(null, buildPageable());

        assertFalse(books.isEmpty());
        assertThat(books.getTotalElements()).isEqualTo(2);
        assertTrue(books.getContent().contains(BookCreator.bookJavaPersisted()));
        assertTrue(books.getContent().contains(BookCreator.bookGolangPersisted()));
    }

    @Test
    @DisplayName("should_return_book_list_find_by_author")
    public void listBookByAuthor() {
        PageImpl<Book> oneBooksPage = new PageImpl<>(List.of(toModel(BookCreator.bookGolangPersisted())), buildPageable(), 1);

        Mockito.when(bookRepository.findByAuthorContaining(anyString(), any(PageRequest.class))).thenReturn(oneBooksPage);

        Page<BookResponse> books = bookService.listBooks("Joshua", buildPageable());

        assertFalse(books.isEmpty());
        assertThat(books.getTotalElements()).isEqualTo(1);
        assertTrue(books.getContent().contains(BookCreator.bookGolangPersisted()));
    }

    @Test
    @DisplayName("should_return_empty_list_books_find_by_author_nonexistent")
    public void emptyBookListByAuthorNonexistent() {
        PageImpl<Book> emptyPage = new PageImpl<>(Collections.emptyList(), buildPageable(), 0);

        Mockito.when(bookRepository.findByAuthorContaining(eq("xxxxx"), any(PageRequest.class))).thenReturn(emptyPage);
        Page<BookResponse> products = bookService.listBooks("xxxxx", buildPageable());

        assertTrue(products.isEmpty());
        assertThat(products.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("should_return_book_find_by_id")
    public void getBookById() {
        Mockito.when(bookRepository.findById(2L)).thenReturn(Optional.of(toModel(BookCreator.bookGolangToBeSaved())));
        BookResponse book = bookService.getBookById(2L);

        assertThat(book).isNotNull();
        assertThat(book.getTitle()).isEqualTo("The Go Programming Language");
    }

    @Test
    @DisplayName("should_return_not_found_search_book_by_id_nonexistent")
    public void getBookByIdNotFound() {
        Mockito.when(bookRepository.findById(10000000L)).thenThrow(new ObjectNotFoundException("Book not found"));

        Throwable exception = catchThrowable(() -> bookService.getBookById(10000000L));

        assertThat(exception)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("should_save_book_successfully")
    public void createBook() throws Exception {
        Mockito.when(bookRepository.existsByIsbn(anyString())).thenReturn(false);
        Mockito.when(bookRepository.existsByTitle(anyString())).thenReturn(false);
        Mockito.when(bookRepository.save(any(Book.class))).thenReturn(toModel(BookCreator.bookJavaPersisted()));

        BookRequest bookRequest = BookCreator.bookJavaToBeSaved();
        BookResponse response = bookService.createBook(bookRequest);

        assertThat(response).isNotNull();
        assertThat(response.getBookId()).isNotNull();
        assertThat(response.getTitle()).isEqualTo("Effective Java");
    }

    @Test
    @DisplayName("should_not_save_a_book_with_duplicated_isbn")
    public void createBookDuplicatedIsbn() {
        Mockito.when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        BookRequest bookRequest = BookCreator.bookJavaToBeSaved();
        Throwable exception = catchThrowable(() -> bookService.createBook(bookRequest));

        assertThat(exception)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Isbn already used");

        Mockito.verify(bookRepository, Mockito.never()).save(toModel(bookRequest));
    }

    @Test
    @DisplayName("should_not_save_a_book_with_duplicated_title")
    public void createBookDuplicatedTitle() {
        Mockito.when(bookRepository.existsByTitle(anyString())).thenReturn(true);

        BookRequest bookRequest = BookCreator.bookJavaToBeSaved();
        Throwable exception = catchThrowable(() -> bookService.createBook(bookRequest));

        assertThat(exception)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Title already used");

        Mockito.verify(bookRepository, Mockito.never()).save(toModel(bookRequest));
    }

    @Test
    @DisplayName("should_update_book_successfully")
    public void updateBook() throws Exception {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(toModel(BookCreator.bookGolangPersisted())));
        Mockito.when(bookRepository.save(any())).thenReturn(toModel(BookCreator.bookGolangUpdatedPersisted()));

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();

        BookResponse bookResponse = bookService.updateBook(updateBookRequest, 1L);

        assertThat(bookResponse).isNotNull();
        assertThat(bookResponse.getBookId()).isNotNull();
        assertThat(bookResponse.getBookId()).isEqualTo(BookCreator.bookGolangUpdatedPersisted().getBookId());
        assertThat(bookResponse.getTitle()).isEqualTo("The Go Programming Language - Updated");
    }

    @Test
    @DisplayName("should_not_update_book_with_title_duplicated")
    public void updateBookTitleDuplicated() {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(toModel(BookCreator.bookGolangPersisted())));
        Mockito.when(bookRepository.existsByTitle(anyString())).thenReturn(true);

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();

        Throwable exception = catchThrowable(() -> bookService.updateBook(updateBookRequest, 1L));

        assertThat(exception)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Title already used");
    }

    @Test
    @DisplayName("should_not_update_book_with_isbn_duplicated")
    public void updateBookIsbnDuplicated() {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(toModel(BookCreator.bookGolangPersisted())));
        Mockito.when(bookRepository.existsByIsbn(anyString())).thenReturn(true);

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();
        updateBookRequest.setIsbn("test");

        Throwable exception = catchThrowable(() -> bookService.updateBook(updateBookRequest, 1L));

        assertThat(exception)
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessage("Isbn already used");
    }

    @Test
    @DisplayName("should_not_update_book_with_book_id_not_found")
    public void updateBookWithBookIdNotFound() {
        Mockito.when(bookRepository.findById(1L)).thenThrow(new ObjectNotFoundException("Book not found"));

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();

        Throwable exception = catchThrowable(() -> bookService.updateBook(updateBookRequest, 1L));

        assertThat(exception)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Book not found");
    }

    @Test
    @DisplayName("should_delete_book_successfully")
    public void deleteBook() {
        Mockito.when(bookRepository.findById(1L)).thenReturn(Optional.of(toModel(BookCreator.bookGolangPersisted())));
        Mockito.doNothing().when(bookRepository).deleteById(eq(1L));

        bookService.deleteBook(1L);
    }

    @Test
    @DisplayName("should_return_not_found_on_delete_with_id_nonexistent")
    public void deleteBookWithBookIdNotFound() {
        Mockito.when(bookRepository.findById(1L)).thenThrow(new ObjectNotFoundException("Book not found"));

        Throwable exception = catchThrowable(() -> bookService.deleteBook(1L));

        assertThat(exception)
                .isInstanceOf(ObjectNotFoundException.class)
                .hasMessage("Book not found");
    }

    private Book toModel(BookResponse bookResponse) {
        return mapper.map(bookResponse, Book.class);
    }

    private Book toModel(BookRequest bookRequest) {
        return mapper.map(bookRequest, Book.class);
    }

    private Pageable buildPageable() {
        return PageRequest.of(0, 10, Sort.Direction.ASC, "bookId");
    }

}






























