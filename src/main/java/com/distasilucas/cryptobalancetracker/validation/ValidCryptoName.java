package com.distasilucas.cryptobalancetracker.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = CryptoNameValidator.class)
@Target(FIELD)
@Retention(RUNTIME)
public @interface ValidCryptoName {
    String message() default "Invalid crypto name";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
