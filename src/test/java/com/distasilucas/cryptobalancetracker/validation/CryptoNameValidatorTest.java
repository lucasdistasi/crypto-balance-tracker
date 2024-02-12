package com.distasilucas.cryptobalancetracker.validation;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CryptoNameValidatorTest {

    private final CryptoNameValidator cryptoNameValidator = new CryptoNameValidator();

    @ParameterizedTest
    @ValueSource(strings = {
        "bitcoin", "BITCOIN", "b1tc0in", "x", "7FbA3d9E1C6gH2jL5M0nR8kPqY4sT1vU3W6xZ9cE2aB4dF7hJ0mN5pQ8rK2tV3yx", "yearn.finance"
    })
    void shouldReturnTrueWhenValidatingCryptoName(String cryptoName) {
        var isValid = cryptoNameValidator.isValid(cryptoName, null);

        assertTrue(isValid);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "", " ", " bitcoin", "bitcoin ", "bit  coin", "$", "x  ", "  x",
        "bit..coin", "7FbA3d9E1C6gH2jL5M0nR8kPqY4sT1vU3W6xZ9cE2aB4dF7hJ0mN5pQ8rK2tV3yxz"
    })
    void shouldReturnFalseWhenValidatingCryptoName(String cryptoName) {
        var isValid = cryptoNameValidator.isValid(cryptoName, null);

        assertFalse(isValid);
    }

}
