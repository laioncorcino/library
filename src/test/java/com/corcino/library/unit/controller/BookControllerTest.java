package com.corcino.library.unit.controller;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;
import com.corcino.library.error.exception.ObjectNotFoundException;
import com.corcino.library.service.BookService;
import com.corcino.library.util.BookCreator;
import com.corcino.library.util.JsonUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest extends ControllerTest {

    private static final String BOOK_API = "http://localhost/api/v1/book";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    @DisplayName("should_return_paginated_list_of_books")
    public void listBookPaginated() throws Exception {
        List<BookResponse> bookList = Arrays.asList(BookCreator.bookJavaPersisted(), (BookCreator.bookGolangPersisted()));
        PageImpl<BookResponse> bookListPaginated = new PageImpl<>(bookList, buildPageable(), 2);

        BDDMockito.when(bookService.listBooks(any(), any())).thenReturn(bookListPaginated);

        MockHttpServletRequestBuilder request = configureGetRequestWithQueryString("/");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json("{'totalElements': 2}"))
                .andExpect(content().json("{'size': 10}"))
                .andExpect(content().string(containsString("The Go Programming Language")))
                .andExpect(content().string(containsString("Effective Java")));
    }

    @Test
    @DisplayName("should_return_paginated_list_of_books_find_by_author")
    public void listBookByAuthor() throws Exception {
        PageImpl<BookResponse> oneBookPaginated = new PageImpl<>(List.of(BookCreator.bookJavaPersisted()), buildPageable(), 1);
        BDDMockito.when(bookService.listBooks(eq("Joshua"), any())).thenReturn(oneBookPaginated);

        MockHttpServletRequestBuilder request = configureGetRequestWithQueryString("?author=Joshua");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json("{'totalElements': 1}"))
                .andExpect(content().json("{'size': 10}"))
                .andExpect(content().string(containsString("Effective Java")));
    }

    @Test
    @DisplayName("should_return_empty_list_books_find_by_author_nonexistent")
    public void emptyBookListByAuthorNonexistent() throws Exception {
        PageImpl<BookResponse> emptyPage = new PageImpl<>(Collections.emptyList(), buildPageable(), 0);
        BDDMockito.when(bookService.listBooks(eq("xxxxx"), any())).thenReturn(emptyPage);

        MockHttpServletRequestBuilder request = configureGetRequestWithQueryString("?author=xxxxx");

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(content().json("{'totalElements': 0}"))
                .andExpect(content().json("{'size': 10}"));
    }

    @Test
    @DisplayName("should_return_book_find_by_id")
    public void getBookById() throws Exception {
        BDDMockito.when(bookService.getBookById(eq(1L))).thenReturn(BookCreator.bookJavaPersisted());

        MockHttpServletRequestBuilder request = configureGetRequestWithId(1L);

        mockMvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    @DisplayName("should_return_not_found_search_book_by_id_nonexistent")
    public void getBookByIdNotFound() throws Exception {
        Mockito.when(bookService.getBookById(eq(10000L))).thenThrow(ObjectNotFoundException.class);

        MockHttpServletRequestBuilder request = configureGetRequestWithId(10000L);

        mockMvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(content().json("{'title': 'Object Not Found Exception. Check documentation'}"));
    }

    @Test
    @DisplayName("should_create_a_book_successfully")
    public void createBook() throws Exception {
        BookResponse bookPersisted = BookCreator.bookGolangPersisted();
        BDDMockito.given(bookService.createBook(Mockito.any(BookRequest.class))).willReturn(bookPersisted);

        String bookRequestJson = JsonUtil.toJson(BookCreator.bookGolangToBeSaved());
        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",BOOK_API + "/1"));
    }

    @Test
    @DisplayName("should_throw_a_validation_error_when_there_is_not_enough_data_to_create_the_book")
    public void validateEmptyRequest() throws Exception {
        String bookRequestJson = JsonUtil.toJson(new BookRequest());
        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    @DisplayName("should_not_save_a_book_with_duplicated_isbn")
    public void createBookDuplicatedIsbn() throws Exception {
        String bookRequestJson = JsonUtil.toJson(BookCreator.bookGolangToBeSaved());
        BDDMockito.given(bookService.createBook(Mockito.any(BookRequest.class))).willThrow(new DataIntegrityViolationException("Isbn already used"));

        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Isbn already used"));
    }

    @Test
    @DisplayName("should_not_save_a_book_with_duplicated_title")
    public void createBookDuplicatedTitle() throws Exception {
        String bookRequestJson = JsonUtil.toJson(BookCreator.bookGolangToBeSaved());
        BDDMockito.given(bookService.createBook(Mockito.any(BookRequest.class))).willThrow(new DataIntegrityViolationException("Title already used"));

        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Title already used"));
    }

    @Test
    @DisplayName("should_update_book_successfully")
    public void updateBook() throws Exception {
        Mockito.when(bookService.updateBook(any(UpdateBookRequest.class), eq(1L))).thenReturn(BookCreator.bookGolangUpdatedPersisted());

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();
        String updateBookRequestJson = JsonUtil.toJson(updateBookRequest);

        MockHttpServletRequestBuilder putRequest = configurePutRequest(1L, updateBookRequestJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", BOOK_API + "/1"));
    }

    @Test
    @DisplayName("should_not_update_book_with_book_id_not_found")
    public void updateBookWithBookIdNotFound() throws Exception {
        Mockito.when(bookService.updateBook(any(UpdateBookRequest.class), eq(100000L))).thenThrow(ObjectNotFoundException.class);

        String updateBookRequestJson = JsonUtil.toJson(new UpdateBookRequest());
        MockHttpServletRequestBuilder putRequest = configurePutRequest(100000L, updateBookRequestJson);

        mockMvc.perform(putRequest)
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should_delete_book_successfully")
    public void deleteBook() throws Exception {
        Mockito.doNothing().when(bookService).deleteBook(eq(2L));

        MockHttpServletRequestBuilder deleteRequest = configureDeleteRequestWithId();

        mockMvc.perform(deleteRequest)
                .andExpect(status().isNoContent());
    }

}
















































