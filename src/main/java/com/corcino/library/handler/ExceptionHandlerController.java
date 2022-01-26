package com.corcino.library.handler;

import com.corcino.library.error.StandardError;
import com.corcino.library.error.ValidationError;
import com.corcino.library.error.exception.ObjectNotFoundException;
import com.corcino.library.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class ExceptionHandlerController extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ObjectNotFoundException.class)
    public ResponseEntity<StandardError> handleNotFound(ObjectNotFoundException notFoundException) {
        return new ResponseEntity<>(
                StandardError.builder()
                        .title("Object Not Found Exception. Check documentation")
                        .status(HttpStatus.NOT_FOUND.value())
                        .errorMessage(notFoundException.getMessage())
                        .developerMessage(notFoundException.getClass().getName())
                        .dateTime(getDateTime())
                        .build(), HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<StandardError> handleConflict(DataIntegrityViolationException conflict) {
        return new ResponseEntity<>(
                StandardError.builder()
                        .title(conflict.getMessage())
                        .status(HttpStatus.CONFLICT.value())
                        .errorMessage(conflict.getMessage())
                        .developerMessage(conflict.getClass().getName())
                        .dateTime(getDateTime())
                        .build(), HttpStatus.CONFLICT
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<StandardError> handleInternalException(Exception exception) {
        return new ResponseEntity<>(
                StandardError.builder()
                        .title("Internal error in server")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorMessage("Internal error in server")
                        .developerMessage(exception.getClass().getName())
                        .dateTime(getDateTime())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Error.class)
    public ResponseEntity<StandardError> handleInternalError(Error error) {
        return new ResponseEntity<>(
                StandardError.builder()
                        .title("Internal error in server ")
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .errorMessage("Internal error in server")
                        .developerMessage(error.getClass().getName())
                        .dateTime(getDateTime())
                        .build(), HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception, HttpHeaders headers, HttpStatus status, WebRequest request) {
        List<ValidationError> standardErrors = new ArrayList<>();
        List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();

        fieldErrors.forEach(fieldError -> {
            standardErrors.add(ValidationError.builder()
                            .title("Bad Request Exception. Invalid fields.")
                            .status(status.value())
                            .errorMessage(fieldError.getDefaultMessage())
                            .developerMessage(exception.getClass().getName())
                            .dateTime(getDateTime())
                            .field(fieldError.getField())
                            .build()
            );
            log.error("Erro de validação no campo " + fieldError.getField() + " para se criar ou atualizar recurso");
        });

        return new ResponseEntity<>(standardErrors, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        StandardError standardError = StandardError.builder()
                .title("Method not allowed. Check documentation")
                .status(status.value())
                .errorMessage(ex.getMessage())
                .developerMessage(ex.getClass().getName())
                .dateTime(getDateTime())
                .build();
        log.error("Method not allowed " + ex.getClass());
        return new ResponseEntity<>(standardError, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object body, HttpHeaders headers,
                                                             HttpStatus status, WebRequest request) {
        StandardError standardError = StandardError.builder()
                .title(exception.getCause().getMessage())
                .status(status.value())
                .errorMessage(exception.getMessage())
                .developerMessage(exception.getClass().getName())
                .dateTime(getDateTime())
                .build();
        log.error("Internal error in server " + exception.getClass());
        return new ResponseEntity<>(standardError, headers, status);
    }

    private String getDateTime() {
        return DateUtil.formatLocalDateTime(LocalDateTime.now());
    }

}
