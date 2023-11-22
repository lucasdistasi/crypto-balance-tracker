package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPlatformException;
import com.distasilucas.cryptobalancetracker.exception.PlatformNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.distasilucas.cryptobalancetracker.constants.Constants.UNKNOWN_ERROR;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.PLATFORM_ID_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;

class ExceptionControllerTest {

    private final MockHttpServletRequest httpServletRequest = new MockHttpServletRequest("POST", "/api/v1/platforms");
    private final ServletWebRequest servletRequest = new ServletWebRequest(httpServletRequest);
    private final ExceptionController exceptionController = new ExceptionController();

    @Test
    void shouldHandlePlatformNotFoundException() {
        var message = PLATFORM_ID_NOT_FOUND.formatted("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");
        var exception = new PlatformNotFoundException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handlePlatformNotFoundException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleDuplicatedPlatformException() {
        var message = DUPLICATED_PLATFORM.formatted("binance");
        var exception = new DuplicatedPlatformException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleDuplicatedPlatformException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleConstraintViolationException() {
        var constraintViolation = ConstraintViolationImpl.forBeanValidation(
                "messageTemplate",
                Collections.emptyMap(),
                Collections.emptyMap(),
                "Some error occurred",
                Platform.class,
                new Platform("id", "name"),
                null,
                null,
                null,
                null,
                null
        );
        var constraintViolationException = new ConstraintViolationException("ConstraintViolationException", Set.of(constraintViolation));
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Some error occurred");
        problemDetail.setType(URI.create(httpServletRequest.getRequestURL().toString()));

        var responseEntity = exceptionController.handleConstraintViolationException(constraintViolationException, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleMethodArgumentNotValidException() {
        var bindException = new BindException("target", "objectName");
        bindException.addError(new ObjectError("objectName", "Error Message"));
        var methodParameter = createMethodParameter(String.class, "compareTo", String.class);
        var exception = new MethodArgumentNotValidException(methodParameter, bindException);
        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create(httpServletRequest.getRequestURL().toString()));
        problemDetail.setDetail("Error Message");

        var responseEntity = exceptionController.handleMethodArgumentNotValidException(exception, servletRequest);

        assertThat(responseEntity)
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleMethodArgumentNotValidExceptionWithUnknownError() {
        var bindException = new BindException("target", "objectName");
        bindException.addError(new ObjectError("objectName", null));
        var methodParameter = createMethodParameter(String.class, "compareTo", String.class);
        var exception = new MethodArgumentNotValidException(methodParameter, bindException);
        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create(httpServletRequest.getRequestURL().toString()));
        problemDetail.setDetail(UNKNOWN_ERROR);

        var responseEntity = exceptionController.handleMethodArgumentNotValidException(exception, servletRequest);

        assertThat(responseEntity)
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    private MethodParameter createMethodParameter(
            Class<?> clazz,
            String methodName,
            Class<?> ...parameterTypes
    ) {
        try {
            Method method = clazz.getDeclaredMethod(methodName, parameterTypes);

            return new MethodParameter(method, -1);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}