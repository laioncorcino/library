package com.corcino.library.dto;

import com.corcino.library.model.Book;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BookResponse {

    private Long bookId;
    private String title;
    private String author;
    private String isbn;

    public BookResponse(Book book) {
        this.bookId = book.getBookId();
        this.title = book.getTitle();
        this.author = book.getAuthor();
        this.isbn = book.getIsbn();
    }

    public Page<BookResponse> convertList(Page<Book> books) {
        return books.map(BookResponse::new);
    }

}
