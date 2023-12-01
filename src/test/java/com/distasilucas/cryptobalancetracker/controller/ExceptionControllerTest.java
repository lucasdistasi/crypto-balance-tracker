package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.entity.Platform;
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
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.context.request.ServletWebRequest;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.distasilucas.cryptobalancetracker.constants.Constants.UNKNOWN_ERROR;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.COINGECKO_CRYPTO_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_CRYPTO_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_GOAL;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.GOAL_ID_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.NOT_ENOUGH_BALANCE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.PLATFORM_ID_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.REQUEST_LIMIT_REACHED;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USERNAME_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USER_CRYPTO_ID_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void shouldHandleGoalNotFoundException() {
        var message = GOAL_ID_NOT_FOUND.formatted("10e3c7c1-0732-4294-9410-9708a21128e3");
        var exception = new GoalNotFoundException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleGoalNotFoundException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleDuplicatedGoalException() {
        var message = DUPLICATED_GOAL.formatted("bitcoin");
        var exception = new DuplicatedGoalException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleDuplicatedGoalException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleCoingeckoCryptoNotFoundException() {
        var message = COINGECKO_CRYPTO_NOT_FOUND.formatted("meme");
        var exception = new CoingeckoCryptoNotFoundException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleCoingeckoCryptoNotFoundException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleDuplicatedCryptoPlatFormException() {
        var message = DUPLICATED_CRYPTO_PLATFORM.formatted("bitcoin", "binance");
        var exception = new DuplicatedCryptoPlatFormException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleDuplicatedCryptoPlatFormException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleUserCryptoNotFoundException() {
        var message = USER_CRYPTO_ID_NOT_FOUND.formatted("meme");
        var exception = new UserCryptoNotFoundException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleUserCryptoNotFoundException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleTooManyRequestsException() {
        var exception = new TooManyRequestsException();
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.TOO_MANY_REQUESTS, REQUEST_LIMIT_REACHED);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleTooManyRequestsException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleInsufficientBalanceException() {
        var exception = new InsufficientBalanceException();
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, NOT_ENOUGH_BALANCE);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleInsufficientBalanceException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleUsernameNotFoundException() {
        var message = USERNAME_NOT_FOUND.formatted("admin");
        var exception = new UsernameNotFoundException(message);
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, message);
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleUsernameNotFoundException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleApiValidationException() {
        var exception = new ApiValidationException(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot!");
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot!");
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleApiValidationException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleApiExceptionWithCustomStatus() {
        var exception = new ApiException(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot!");
        var problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.I_AM_A_TEAPOT, "I'm a teapot!");
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleApiException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.I_AM_A_TEAPOT).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleApiExceptionWithInternalServerError() {
        var runtimeException = new RuntimeException("Some exception as occurred");
        var exception = new ApiException(UNKNOWN_ERROR, runtimeException);
        var problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                UNKNOWN_ERROR
        );
        problemDetail.setType(URI.create(servletRequest.getRequest().getRequestURL().toString()));

        var responseEntity = exceptionController.handleApiException(exception, servletRequest);

        assertThat(responseEntity)
                .usingRecursiveComparison()
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(problemDetail)));
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

    @Test
    void shouldHandleMissingServletRequestParameterException() {
        var exception = new MissingServletRequestParameterException("parameterName", "parameterType");
        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create(httpServletRequest.getRequestURL().toString()));
        problemDetail.setDetail("Required parameter 'parameterName' is not present.");

        var responseEntity = exceptionController.handleMissingServletRequestParameterException(exception, servletRequest);

        assertThat(responseEntity)
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleMissingServletRequestParameterExceptionWithNullDetail() {
        var exceptionMock = mock(MissingServletRequestParameterException.class);
        var nullProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.I_AM_A_TEAPOT, null);

        when(exceptionMock.getBody()).thenReturn(nullProblemDetail);
        when(exceptionMock.getMessage()).thenReturn("Some error occurred");

        var problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setType(URI.create(httpServletRequest.getRequestURL().toString()));
        problemDetail.setDetail("Some error occurred");

        var responseEntity = exceptionController.handleMissingServletRequestParameterException(exceptionMock, servletRequest);

        assertThat(responseEntity)
                .isEqualTo(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of(problemDetail)));
    }

    @Test
    void shouldHandleException() {
        var exception = new Exception("Some exception has occurred");
        var problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setType(URI.create(httpServletRequest.getRequestURL().toString()));
        problemDetail.setDetail(UNKNOWN_ERROR);

        var responseEntity = exceptionController.handleException(exception, servletRequest);

        assertThat(responseEntity)
                .isEqualTo(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of(problemDetail)));
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