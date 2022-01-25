package com.corcino.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookRequest {

    @NotEmpty(message = "Title is mandatory")
    private String title;

    @NotEmpty(message = "Author is mandatory")
    private String author;

    @NotEmpty(message = "Isbn is mandatory")
    private String isbn;

}
