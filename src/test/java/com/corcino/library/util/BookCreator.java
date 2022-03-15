package com.corcino.library.util;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;

public class BookCreator {

    public static BookRequest bookGolangToBeSaved() {
        return BookRequest.builder()
                .title("The Go Programming Language")
                .author("Alan A.A. Donovan")
                .isbn("9780134190440")
                .build();
    }

    public static BookResponse bookGolangPersisted() {
        return BookResponse.builder()
                .bookId(1L)
                .title("The Go Programming Language")
                .author("Alan A.A. Donovan")
                .isbn("9780134190440")
                .build();
    }

    public static BookResponse bookGolangUpdatedPersisted() {
        return BookResponse.builder()
                .bookId(1L)
                .title("The Go Programming Language - Updated")
                .author("Alan A.A. Donovan")
                .isbn("9780134190440")
                .build();
    }

    public static UpdateBookRequest bookGolangToUpdate() {
        UpdateBookRequest updateBookRequest = new UpdateBookRequest();
        updateBookRequest.setTitle("The Go Programming Language - Updated");
        return updateBookRequest;
    }

    public static BookRequest bookJavaToBeSaved() {
        return BookRequest.builder()
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("0134685997")
                .build();
    }

    public static BookResponse bookJavaPersisted() {
        return BookResponse.builder()
                .bookId(2L)
                .title("Effective Java")
                .author("Joshua Bloch")
                .isbn("0134685997")
                .build();
    }

}
