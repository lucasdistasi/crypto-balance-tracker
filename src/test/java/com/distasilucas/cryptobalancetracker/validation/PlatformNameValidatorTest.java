package com.distasilucas.cryptobalancetracker.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformNameValidatorTest {

    private final PlatformNameValidator platformNameValidator = new PlatformNameValidator();

    @ParameterizedTest
    @ValueSource(strings = {"binance", "OKX", "Kraken", "Safepal", "Coinbase"})
    void shouldReturnTrueWhenValidatingPlatformName(String platformName) {
        var isValid = platformNameValidator.isValid(platformName, null);

        assertTrue(isValid);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
            "INVALID PLATFORM", "INVALID-PLATFORM", "INVALID_PLATFORM", "LOOOOOOONGINVALIDPLATFORM", "", "1NV4L1D"
    })
    void shouldReturnFalseWhenValidatingPlatformName(String platformName) {
        var isValid = platformNameValidator.isValid(platformName, null);

        assertFalse(isValid);
    }

}