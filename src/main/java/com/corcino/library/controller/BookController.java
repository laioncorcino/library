package com.corcino.library.controller;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;
import com.corcino.library.service.BookService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;

import static org.springframework.data.domain.Sort.Direction.ASC;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/book")
public class BookController {

    private BookService bookService;

    @GetMapping
    public ResponseEntity<Page<BookResponse>> list(@RequestParam(required = false) String author,
                                                   @PageableDefault(sort = "bookId", direction = ASC) Pageable pageable) {
        Page<BookResponse> books = bookService.listBooks(author, pageable);
        return ResponseEntity.ok(books);
    }

    @GetMapping("/{bookId}")
    public ResponseEntity<BookResponse> getById(@PathVariable Long bookId) {
        BookResponse bookResponse = bookService.getBookById(bookId);
        return ResponseEntity.ok(bookResponse);
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody @Valid BookRequest bookRequest, UriComponentsBuilder uriBuilder) throws Exception {
        BookResponse bookResponse = bookService.createBook(bookRequest);
        URI uri = uriBuilder.path("/api/v1/book/{bookId}").buildAndExpand(bookResponse.getBookId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @PutMapping("/{bookId}")
    public ResponseEntity<String> update(@RequestBody @Valid UpdateBookRequest updateBook, @PathVariable Long bookId,
                                         UriComponentsBuilder uriBuilder) throws Exception {
        BookResponse bookResponse = bookService.updateBook(updateBook, bookId);
        URI uri = uriBuilder.path("/api/v1/book/{bookId}").buildAndExpand(bookResponse.getBookId()).toUri();
        return ResponseEntity.created(uri).build();
    }

    @DeleteMapping("/{bookId}")
    public ResponseEntity<Void> delete(@PathVariable Long bookId) {
        bookService.deleteBook(bookId);
        return ResponseEntity.noContent().build();
    }

}







































