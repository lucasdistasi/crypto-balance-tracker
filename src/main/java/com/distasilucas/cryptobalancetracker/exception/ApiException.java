package com.distasilucas.cryptobalancetracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String message;

    public ApiException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public ApiException(String message, Throwable exception) {
        super(message, exception);
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = message;
    }
}
