package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.exception.ApiException;
import com.distasilucas.cryptobalancetracker.exception.ApiValidationException;
import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedCryptoPlatFormException;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedGoalException;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPlatformException;
import com.distasilucas.cryptobalancetracker.exception.GoalNotFoundException;
import com.distasilucas.cryptobalancetracker.exception.InsufficientBalanceException;
import com.distasilucas.cryptobalancetracker.exception.PlatformNotFoundException;
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException;
import com.distasilucas.cryptobalancetracker.exception.UserCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.exception.UsernameNotFoundException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.util.Arrays;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.UNKNOWN_ERROR;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.INVALID_VALUE_FOR;

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

    @ExceptionHandler(GoalNotFoundException.class)
    public ResponseEntity<List<ProblemDetail>> handleGoalNotFoundException(
        GoalNotFoundException exception,
        WebRequest webRequest
    ) {
        log.info("A GoalNotFoundException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(NOT_FOUND_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(DuplicatedGoalException.class)
    public ResponseEntity<List<ProblemDetail>> handleDuplicatedGoalException(
        DuplicatedGoalException exception,
        WebRequest webRequest
    ) {
        log.info("A DuplicatedGoalException has occurred", exception);

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

    @ExceptionHandler(TooManyRequestsException.class)
    public ResponseEntity<List<ProblemDetail>> handleTooManyRequestsException(
        TooManyRequestsException exception,
        WebRequest webRequest
    ) {
        log.info("A TooManyRequestsException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(List.of(problemDetail));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<List<ProblemDetail>> handleInsufficientBalanceException(
        InsufficientBalanceException exception,
        WebRequest webRequest
    ) {
        log.info("An InsufficientBalanceException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail));
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<List<ProblemDetail>> handleUsernameNotFoundException(
        UsernameNotFoundException exception,
        WebRequest webRequest
    ) {
        log.info("An UsernameNotFoundException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(NOT_FOUND_STATUS, exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(NOT_FOUND_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(ApiValidationException.class)
    public ResponseEntity<List<ProblemDetail>> handleApiValidationException(
        ApiValidationException exception,
        WebRequest webRequest
    ) {
        log.info("An ApiValidationException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(exception.getHttpStatus(), exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(exception.getHttpStatus()).body(List.of(problemDetail));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<List<ProblemDetail>> handleApiException(
        ApiException exception,
        WebRequest webRequest
    ) {
        log.info("An ApiException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(exception.getHttpStatus(), exception.getMessage());
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(exception.getHttpStatus()).body(List.of(problemDetail));
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

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<List<ProblemDetail>> handleMissingServletRequestParameterException(
        MissingServletRequestParameterException exception,
        WebRequest webRequest
    ) {
        log.info("A MissingServletRequestParameterException has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var bodyDetail = exception.getBody().getDetail();
        var detail = StringUtils.hasText(bodyDetail) ? bodyDetail : exception.getMessage();
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST_STATUS, detail);
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(BAD_REQUEST_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<List<ProblemDetail>> handleMethodArgumentTypeMismatchException(
        MethodArgumentTypeMismatchException exception,
        WebRequest webRequest
    ) {
        log.info("A MethodArgumentTypeMismatchException has occurred", exception);

        var name = exception.getName();
        var message = String.format("Invalid value %s for %s", exception.getValue(), name);
        var requiredType = exception.getRequiredType();

        if (requiredType != null) {
            var availableValues = Arrays.toString(requiredType.getEnumConstants());
            message = String.format(INVALID_VALUE_FOR, exception.getValue(), name, availableValues);
        }

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(BAD_REQUEST_STATUS, message);
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(BAD_REQUEST_STATUS).body(List.of(problemDetail));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<List<ProblemDetail>> handleException(
        Exception exception,
        WebRequest webRequest
    ) {
        log.error("An unhandled Exception has occurred", exception);

        var request = (ServletWebRequest) webRequest;
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, UNKNOWN_ERROR);
        problemDetail.setType(URI.create(request.getRequest().getRequestURL().toString()));

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(problemDetail));
    }
}
