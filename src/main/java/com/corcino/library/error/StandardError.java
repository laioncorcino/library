package com.corcino.library.error;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class StandardError {

    protected String title;
    protected int status;
    protected String errorMessage;
    protected String developerMessage;
    protected String dateTime;

}
