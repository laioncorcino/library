package com.corcino.library.util;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;

public class BookCreator {

    public static BookRequest createBookGolangToBeSaved() {
        return BookRequest.builder()
                .title("The Go Programming Language")
                .author("Alan A.A. Donovan")
                .isbn("9780134190440")
                .build();
    }

    public static BookResponse createBookGolangPersisted() {
        return BookResponse.builder()
                .bookId(1L)
                .title("The Go Programming Language")
                .authors("Alan A.A. Donovan")
                .isbn("9780134190440")
                .build();
    }

    public static BookRequest createBookJavaToBeSaved() {
        return BookRequest.builder()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("0134685997")
                .build();
    }

    public static BookResponse createBookJavaPersisted() {
        return BookResponse.builder()
                .bookId(2L)
                .title("Effective Java")
                .authors("Joshua Bloch")
                .isbn("0134685997")
                .build();
    }


}
