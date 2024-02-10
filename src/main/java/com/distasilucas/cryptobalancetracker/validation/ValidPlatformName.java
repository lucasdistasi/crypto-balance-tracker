package com.distasilucas.cryptobalancetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PLATFORM_NAME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = PlatformNameValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface ValidPlatformName {
    String message() default INVALID_PLATFORM_NAME;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
