package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionConstants {

    public static final String PLATFORM_ID_NOT_FOUND = "Platform with id %s not found";
    public static final String DUPLICATED_PLATFORM = "Platform %s already exists";
    public static final String COINGECKO_CRYPTO_NOT_FOUND = "Coingecko crypto with name %s not found";
    public static final String USER_CRYPTO_ID_NOT_FOUND = "User crypto with id %s not found";
    public static final String DUPLICATED_CRYPTO_PLATFORM = "You already have %s in %s";
}
