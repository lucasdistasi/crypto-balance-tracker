package com.distasilucas.cryptobalancetracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiValidationException extends RuntimeException {

    private final HttpStatus httpStatus;

    public ApiValidationException(HttpStatus status, String message) {
        super(message);
        this.httpStatus = status;
    }
}
