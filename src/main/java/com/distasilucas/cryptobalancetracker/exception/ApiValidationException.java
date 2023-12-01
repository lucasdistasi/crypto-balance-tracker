package com.distasilucas.cryptobalancetracker.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiValidationException extends ApiException {

    private final HttpStatus httpStatus;

    public ApiValidationException(HttpStatus status, String message) {
        super(status, message);
        this.httpStatus = status;
    }
}
