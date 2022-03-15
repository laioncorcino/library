package com.corcino.library.unit.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

public class ControllerTest {

    private static final String BOOK_API = "http://localhost/api/v1/book";

    protected MockHttpServletRequestBuilder configureGetRequestWithId(Long bookId) {
        return get(BOOK_API + "/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    protected MockHttpServletRequestBuilder configureGetRequestWithQueryString(String query) {
        return get(BOOK_API + query)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    protected MockHttpServletRequestBuilder configurePostRequest(String bookRequestJson) {
        return post(BOOK_API)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookRequestJson);
    }

    protected MockHttpServletRequestBuilder configurePutRequest(Long bookId, String bookRequestJson) {
        return put(BOOK_API + "/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(bookRequestJson);
    }

    protected MockHttpServletRequestBuilder configureDeleteRequestWithId() {
        return delete(BOOK_API + "/{bookId}", 2)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);
    }

    protected Pageable buildPageable() {
        return PageRequest.of(0, 10, Sort.Direction.ASC, "bookId");
    }
}
