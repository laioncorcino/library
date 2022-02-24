package com.corcino.library.unit.controller;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.service.BookService;
import com.corcino.library.util.BookCreator;
import com.corcino.library.util.JsonUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookControllerTest {

    private static final String BOOK_API = "http://localhost/api/v1/book";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookService bookService;

    @Test
    public void should_create_a_book_successfully() throws Exception {
        BookResponse bookPersisted = BookCreator.createBookGolangPersisted();
        BDDMockito.given(bookService.createBook(Mockito.any(BookRequest.class))).willReturn(bookPersisted);

        String bookRequestJson = JsonUtil.toJson(BookCreator.createBookGolangToBeSaved());
        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",BOOK_API + "/1"));
    }

    @Test
    public void should_throw_a_validation_error_when_there_is_not_enough_data_to_create_the_book() throws Exception {
        String bookRequestJson = JsonUtil.toJson(new BookRequest());
        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.length()").value(3));
    }

    @Test
    public void should_throw_error_when_trying_to_register_book_with_isbn_already_used() throws Exception {
        String bookRequestJson = JsonUtil.toJson(BookCreator.createBookGolangToBeSaved());
        BDDMockito.given(bookService.createBook(Mockito.any(BookRequest.class))).willThrow(new DataIntegrityViolationException("Isbn already used"));

        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Isbn already used"));
    }

    @Test
    public void should_throw_error_when_trying_to_register_book_with_title_already_used() throws Exception {
        String bookRequestJson = JsonUtil.toJson(BookCreator.createBookGolangToBeSaved());
        BDDMockito.given(bookService.createBook(Mockito.any(BookRequest.class))).willThrow(new DataIntegrityViolationException("Title already used"));

        MockHttpServletRequestBuilder request = configurePostRequest(bookRequestJson);

        mockMvc.perform(request)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Title already used"));
    }

    private MockHttpServletRequestBuilder configurePostRequest(String bookRequestJson) {
        return post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookRequestJson);
    }

}
















































