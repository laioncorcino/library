package com.corcino.library.integration;

import com.corcino.library.dto.BookRequest;
import com.corcino.library.dto.BookResponse;
import com.corcino.library.dto.UpdateBookRequest;
import com.corcino.library.repository.BookRepository;
import com.corcino.library.util.BookCreator;
import com.corcino.library.wrapper.PageableResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BookSystemTest extends SystemTest {

    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    public void cleanerDatabase() {
        bookRepository.deleteAll();
    }

    @Test
    @DisplayName("should_return_paginated_list_of_books")
    public void listBookPaginated() {
        saveTwoBooksInDatabase();

        ResponseEntity<PageableResponse<BookResponse>> getResponse = doGetPage("/");

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        PageableResponse<BookResponse> pageBooks = getResponse.getBody();

        assertThat(pageBooks).isNotNull();
        assertThat(pageBooks.getNumberOfElements()).isEqualTo(2);
        assertThat(pageBooks.getContent().size()).isEqualTo(2);
        assertThat(pageBooks.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageBooks.getPageable().getPageSize()).isEqualTo(10);

        List<BookResponse> content = pageBooks.getContent();

        assertTrue(content.stream().anyMatch(book -> book.getTitle().equals("Effective Java")));
        assertTrue(content.stream().anyMatch(book -> book.getTitle().equals("The Go Programming Language")));
    }

    @Test
    @DisplayName("should_return_paginated_list_of_books_find_by_author")
    public void listBookByAuthor() {
        saveTwoBooksInDatabase();

        ResponseEntity<PageableResponse<BookResponse>> getResponse = doGetPage("?author=donovan");

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        PageableResponse<BookResponse> pageBooks = getResponse.getBody();

        assertThat(pageBooks).isNotNull();
        assertThat(pageBooks.getNumberOfElements()).isEqualTo(1);
        assertThat(pageBooks.getContent().size()).isEqualTo(1);
        assertThat(pageBooks.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageBooks.getPageable().getPageSize()).isEqualTo(10);

        List<BookResponse> content = pageBooks.getContent();

        assertTrue(content.stream().anyMatch(book -> book.getTitle().equals("The Go Programming Language")));
        assertTrue(content.stream().noneMatch(book -> book.getTitle().equals("Effective Java")));
    }

    @Test
    @DisplayName("should_return_empty_list_books_find_by_author_nonexistent")
    public void emptyBookListByAuthorNonexistent() {
        saveTwoBooksInDatabase();

        ResponseEntity<PageableResponse<BookResponse>> getResponse = doGetPage("?author=not_found");

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        PageableResponse<BookResponse> pageBooks = getResponse.getBody();

        assertThat(pageBooks).isNotNull();
        assertThat(pageBooks.getNumberOfElements()).isEqualTo(0);
        assertThat(pageBooks.getContent().size()).isEqualTo(0);
        assertThat(pageBooks.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(pageBooks.getPageable().getPageSize()).isEqualTo(10);

        List<BookResponse> content = pageBooks.getContent();

        assertTrue(content.stream().noneMatch(book -> book.getTitle().equals("The Go Programming Language")));
        assertTrue(content.stream().noneMatch(book -> book.getTitle().equals("Effective Java")));
    }

    @Test
    @DisplayName("should_return_book_find_by_id")
    public void getBookById() {
        doPost(BookCreator.bookGolangToBeSaved());
        ResponseEntity<String> postResponse = doPost(BookCreator.bookJavaToBeSaved());

        String uri = extractUrlContext(postResponse);
        ResponseEntity<BookResponse> getResponse = doGet(uri);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Effective Java");
    }

    @Test
    @DisplayName("should_return_not_found_search_book_by_id_nonexistent")
    public void getBookByIdNotFound() {
        doPost(BookCreator.bookGolangToBeSaved());

        ResponseEntity<Object> getResponse = doGetBookId(100000L);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNotNull();
        assertTrue(getResponse.getBody().toString().contains("Object Not Found Exception. Check documentation"));
    }

    @Test
    @DisplayName("should_create_a_book_successfully")
    public void createBook() {
        ResponseEntity<String> postResponse = doPost(BookCreator.bookJavaToBeSaved());

        assertThat(postResponse).isNotNull();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResponse.getHeaders().getLocation()).isNotNull();

        String uri = extractUrlContext(postResponse);
        ResponseEntity<BookResponse> getResponse = doGet(uri);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getTitle()).isEqualTo("Effective Java");
    }

    @Test
    @DisplayName("should_throw_a_validation_error_when_there_is_not_enough_data_to_create_the_book")
    public void validateEmptyRequest() {
        ResponseEntity<String> postResponse = doPost(new BookRequest());

        assertThat(postResponse).isNotNull();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        assertThat(postResponse.getBody()).isNotNull();
        assertTrue(postResponse.getBody().contains("Author is mandatory"));
        assertTrue(postResponse.getBody().contains("Isbn is mandatory"));
        assertTrue(postResponse.getBody().contains("Title is mandatory"));
    }

    @Test
    @DisplayName("should_not_save_a_book_with_duplicated_isbn")
    public void createBookDuplicatedIsbn() {
        doPost(BookCreator.bookGolangToBeSaved());
        BookRequest bookRequestWithDuplicatedIsbn = new BookRequest("Clean Architecture", "Robert Martin", "9780134190440");
        ResponseEntity<String> postResponse = doPost(bookRequestWithDuplicatedIsbn);

        assertThat(postResponse).isNotNull();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(postResponse.getBody()).isNotNull();
        assertTrue(postResponse.getBody().contains("Isbn already used"));
    }

    @Test
    @DisplayName("should_not_save_a_book_with_duplicated_title")
    public void createBookDuplicatedTitle() {
        doPost(BookCreator.bookJavaToBeSaved());
        BookRequest bookRequestWithDuplicatedTitle = new BookRequest("Effective Java", "Robert Martin", "9780134190440");
        ResponseEntity<String> postResponse = doPost(bookRequestWithDuplicatedTitle);

        assertThat(postResponse).isNotNull();
        assertThat(postResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(postResponse.getBody()).isNotNull();
        assertTrue(postResponse.getBody().contains("Title already used"));
    }

    @Test
    @DisplayName("should_update_book_successfully")
    public void updateBook() {
        doPost(BookCreator.bookJavaToBeSaved());
        ResponseEntity<String> postResponse = doPost(BookCreator.bookGolangToBeSaved());
        String postResource = extractUrlContext(postResponse);

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();
        ResponseEntity<String> putResponse = doPut(extractUrlContext(postResponse), updateBookRequest);
        String putResource = extractUrlContext(putResponse);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(postResource).isEqualTo(putResource);

        ResponseEntity<BookResponse> getResponse = doGet(putResource);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getTitle()).isEqualTo("The Go Programming Language - Updated");
    }

    @Test
    @DisplayName("should_not_update_book_with_title_duplicated")
    public void updateBookTitleDuplicated() {
        doPost(BookCreator.bookGolangToBeSaved());
        ResponseEntity<String> postResponse = doPost(BookCreator.bookJavaToBeSaved());

        UpdateBookRequest updateBookRequest = new UpdateBookRequest();
        updateBookRequest.setTitle("Effective Java");

        ResponseEntity<String> putResponse = doPut(extractUrlContext(postResponse), updateBookRequest);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(putResponse.getBody()).isNotNull();
        assertTrue(putResponse.getBody().contains("Title already used"));
    }

    @Test
    @DisplayName("should_not_update_book_with_isbn_duplicated")
    public void updateBookIsbnDuplicated() {
        doPost(BookCreator.bookGolangToBeSaved());
        ResponseEntity<String> postResponse = doPost(BookCreator.bookJavaToBeSaved());

        UpdateBookRequest updateBookRequest = new UpdateBookRequest();
        updateBookRequest.setIsbn("0134685997");

        ResponseEntity<String> putResponse = doPut(extractUrlContext(postResponse), updateBookRequest);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(putResponse.getBody()).isNotNull();
        assertTrue(putResponse.getBody().contains("Isbn already used"));
    }

    @Test
    @DisplayName("should_not_update_book_with_book_id_not_found")
    public void updateBookWithBookIdNotFound() {
        saveTwoBooksInDatabase();

        UpdateBookRequest updateBookRequest = BookCreator.bookGolangToUpdate();

        ResponseEntity<String> putResponse = doPut("/api/v1/book/1000000", updateBookRequest);

        assertThat(putResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(putResponse.getBody()).isNotNull();
        assertTrue(putResponse.getBody().contains("Object Not Found Exception. Check documentation"));
    }

    @Test
    @DisplayName("should_delete_book_successfully")
    public void deleteBook() {
        doPost(BookCreator.bookGolangToBeSaved());
        ResponseEntity<String> postResponse = doPost(BookCreator.bookJavaToBeSaved());
        String postResource = extractUrlContext(postResponse);

        ResponseEntity<String> deleteResponse = doDelete(postResource);
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<BookResponse> getResponse = doGet(postResource);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(getResponse.getBody()).isNotNull();
        assertTrue(getResponse.getBody().toString().contains("Object Not Found Exception. Check documentation"));

        ResponseEntity<PageableResponse<BookResponse>> getPage = doGetPage("/");

        assertThat(getPage).isNotNull();

        PageableResponse<BookResponse> pageBooks = getPage.getBody();

        assertThat(pageBooks).isNotNull();
        assertThat(pageBooks.getNumberOfElements()).isEqualTo(1);
        assertThat(pageBooks.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("should_return_not_found_on_delete_with_id_nonexistent")
    public void deleteBookWithBookIdNotFound() {
        saveTwoBooksInDatabase();

        ResponseEntity<String> deleteResponse = doDelete("/api/v1/book/1000000");

        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteResponse.getBody()).isNotNull();
        assertTrue(deleteResponse.getBody().contains("Object Not Found Exception. Check documentation"));
    }

}
