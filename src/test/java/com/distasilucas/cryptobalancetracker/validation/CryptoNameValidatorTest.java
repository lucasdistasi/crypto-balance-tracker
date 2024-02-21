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
        "bitcoin", "BITCOIN", "b1tc0in", "x", "yearn.finance", "$wen", "-some-crypto-", "#test"
    })
    void shouldReturnTrueWhenValidatingCryptoName(String cryptoName) {
        var isValid = cryptoNameValidator.isValid(cryptoName, null);

        assertTrue(isValid);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {
        "", " ", " bitcoin", "bitcoin ", "bit  coin", "x ", " x", "ether  ", "  ether"
    })
    void shouldReturnFalseWhenValidatingCryptoName(String cryptoName) {
        var isValid = cryptoNameValidator.isValid(cryptoName, null);

        assertFalse(isValid);
    }

}
