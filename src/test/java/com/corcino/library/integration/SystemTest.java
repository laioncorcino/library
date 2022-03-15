package com.corcino.library.integration;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;
import com.corcino.library.util.BookCreator;
import com.corcino.library.wrapper.PageableResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class SystemTest {

    private static final String BOOK_API = "/api/v1/book";

    @Autowired
    protected TestRestTemplate testRestTemplate;

    protected ResponseEntity<PageableResponse<BookResponse>> doGetPage(String url) {
        return testRestTemplate.exchange(
                BOOK_API + url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {}
        );
    }

    protected ResponseEntity<Object> doGetBookId(Long bookId) {
        return testRestTemplate.getForEntity(
                BOOK_API + "/" + bookId,
                Object.class
        );
    }

    protected ResponseEntity<BookResponse> doGet(String resource) {
        return testRestTemplate.getForEntity(
                resource,
                BookResponse.class
        );
    }

    protected ResponseEntity<String> doPost(BookRequest book) {
        return testRestTemplate.postForEntity(
                BOOK_API,
                book,
                String.class
        );
    }

    protected ResponseEntity<String> doPut(String resource, UpdateBookRequest updateRequest) {
        return testRestTemplate.exchange(
                resource,
                HttpMethod.PUT,
                new HttpEntity<>(updateRequest, createJsonHeader()),
                String.class
        );
    }

    protected ResponseEntity<String> doDelete(String resource) {
        return testRestTemplate.exchange(
                resource,
                HttpMethod.DELETE,
                null,
                String.class
        );
    }

    protected void saveTwoBooksInDatabase() {
        doPost(BookCreator.bookJavaToBeSaved());
        doPost(BookCreator.bookGolangToBeSaved());
    }

    protected String extractUrlContext(ResponseEntity<String> postResponse) {
        return Objects.requireNonNull(postResponse.getHeaders().getLocation()).getPath();
    }

    protected static HttpHeaders createJsonHeader() {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }

}
