package com.corcino.library.model;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @Column(unique = true)
    private String title;

    private String author;

    @Column(unique = true)
    private String isbn;

}
