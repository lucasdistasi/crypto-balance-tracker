package com.distasilucas.cryptobalancetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_NAME_REGEX;

public class PlatformNameValidator implements ConstraintValidator<ValidPlatformName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.hasText(value) && value.matches(PLATFORM_NAME_REGEX);
    }
}
