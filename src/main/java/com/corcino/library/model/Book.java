package com.corcino.library.model;

import lombok.Data;

import java.util.List;

@Data
public class Book {

    private Long bookId;
    private String title;
    private List<String> authors;
    private String isbn;

}
