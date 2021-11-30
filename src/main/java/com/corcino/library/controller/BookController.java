package com.corcino.library.controller;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/book")
public class BookController {

    private BookService bookService;

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid BookRequest bookRequest, UriComponentsBuilder uriBuilder) {
        BookResponse bookResponse = bookService.createBook(bookRequest);
        URI uri = uriBuilder.path("/api/v1/book/{bookId}").buildAndExpand(bookResponse.getBookId()).toUri();
        return ResponseEntity.created(uri).build();
    }


}







































