package com.corcino.library.util;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;

import java.util.Arrays;
import java.util.List;

public class BookCreator {

    public static BookRequest createBookGolangToBeSaved() {
        return BookRequest.builder()
                .title("The Go Programming Language")
                .authors(Arrays.asList("Alan A.A. Donovan", "Brian W. Kernighan"))
                .isbn("9780134190440")
                .build();
    }

    public static BookResponse createBookGolangPersisted() {
        return BookResponse.builder()
                .bookId(1L)
                .title("The Go Programming Language")
                .authors(Arrays.asList("Alan A.A. Donovan", "Brian W. Kernighan"))
                .isbn("9780134190440")
                .build();
    }

    public static BookRequest createBookJavaToBeSaved() {
        return BookRequest.builder()
                .title("Effective Java")
                .authors(List.of("Joshua Bloch"))
                .isbn("0134685997")
                .build();
    }

    public static BookResponse createBookJavaPersisted() {
        return BookResponse.builder()
                .bookId(2L)
                .title("Effective Java")
                .authors(List.of("Joshua Bloch"))
                .isbn("0134685997")
                .build();
    }


}
