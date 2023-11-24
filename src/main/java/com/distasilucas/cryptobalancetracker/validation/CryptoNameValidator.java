package com.distasilucas.cryptobalancetracker.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.util.StringUtils;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_REGEX;

public class CryptoNameValidator implements ConstraintValidator<ValidCryptoName, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return StringUtils.hasText(value) && value.matches(CRYPTO_NAME_REGEX);
    }
}
