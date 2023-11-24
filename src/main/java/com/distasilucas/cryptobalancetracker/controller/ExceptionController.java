package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedCryptoPlatFormException;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPlatformException;
import com.distasilucas.cryptobalancetracker.exception.PlatformNotFoundException;
import com.distasilucas.cryptobalancetracker.exception.UserCryptoNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.net.URI;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.UNKNOWN_ERROR;

@Slf4j
@RestControllerAdvice
public class ExceptionController {

    private static final HttpStatus NOT_FOUND_STATUS = HttpStatus.NOT_FOUND;
    private static final HttpStatus BAD_REQUEST_STATUS = HttpStatus.BAD_REQUEST;

    @ExceptionHandler(PlatformNotFoundException.class)
    public ResponseEntity<List<ProblemDetail>> handlePlatformNotFoundException(
            PlatformNotFoundException exception,
            WebRequest webRequest
    ) {
        log.info("A PlatformNotFoundException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(NOT_FOUND_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(DuplicatedPlatformException.class)
    public ResponseEntity<List<ProblemDetail>> handleDuplicatedPlatformException(
            DuplicatedPlatformException exception,
            WebRequest webRequest
    ) {
        log.info("A DuplicatedPlatformException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(BAD_REQUEST_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(CoingeckoCryptoNotFoundException.class)
    public ResponseEntity<List<ProblemDetail>> handleCoingeckoCryptoNotFoundException(
            CoingeckoCryptoNotFoundException exception,
            WebRequest webRequest
    ) {
        log.info("A CoingeckoCryptoNotFoundException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(NOT_FOUND_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(DuplicatedCryptoPlatFormException.class)
    public ResponseEntity<List<ProblemDetail>> handleDuplicatedCryptoPlatFormException(
            DuplicatedCryptoPlatFormException exception,
            WebRequest webRequest
    ) {
        log.info("A DuplicatedCryptoPlatFormException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(BAD_REQUEST_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(UserCryptoNotFoundException.class)
    public ResponseEntity<List<ProblemDetail>> handleUserCryptoNotFoundException(
            UserCryptoNotFoundException exception,
            WebRequest webRequest
    ) {
        log.info("An UserCryptoNotFoundException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(NOT_FOUND_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<List<ProblemDetail>> handleConstraintViolationException(
            ConstraintViolationException exception,
            WebRequest webRequest
    ) {
        log.info("A ConstraintViolationException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var constraintViolations = exception.getConstraintViolations().stream().toList();
        var problemDetails = constraintViolations.stream()
                .map(constraintViolation -> {
                    var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST_STATUS, constraintViolation.getMessage());
                    problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

                    return problemDetail;
                })
                .toList();

        return ResponseEntity.status(BAD_REQUEST_STATUS).body(problemDetails);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<List<ProblemDetail>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception,
            WebRequest webRequest
    ) {
        log.info("A MethodArgumentNotValidException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var allErrors = exception.getAllErrors()
                .stream()
                .map(error -> {
                    var errorMessage = StringUtils.hasText(error.getDefaultMessage()) ? error.getDefaultMessage() : UNKNOWN_ERROR;
                    var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST_STATUS, errorMessage);
                    problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

                    return problemDetail;
                })
                .toList();

        return ResponseEntity.status(BAD_REQUEST_STATUS).body(allErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ProblemDetail>> handleException(
            Exception exception,
            WebRequest webRequest
    ) {
        log.warn("An unhandled Exception has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR);
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(problemDetail));
    }
}
