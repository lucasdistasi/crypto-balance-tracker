package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionConstants {

    public static final String PLATFORM_ID_NOT_FOUND = "Platform with id %s not found";
    public static final String DUPLICATED_PLATFORM = "Platform %s already exists";
    public static final String COINGECKO_CRYPTO_NOT_FOUND = "Coingecko crypto %s not found";
    public static final String USER_CRYPTO_ID_NOT_FOUND = "User crypto with id %s not found";
    public static final String DUPLICATED_CRYPTO_PLATFORM = "You already have %s in %s";
    public static final String GOAL_ID_NOT_FOUND = "Goal with id %s not found";
    public static final String DUPLICATED_GOAL = "You already have a goal for %s";
    public static final String REQUEST_LIMIT_REACHED = "Request limit reached";
    public static final String NOT_ENOUGH_BALANCE = "You don't have enough balance to perform this action";
    public static final String SAME_FROM_TO_PLATFORM = "From platform and to platform cannot be the same";
    public static final String TOKEN_EXPIRED = "Token is expired";
    public static final String USERNAME_NOT_FOUND = "Username %s not found";
    public static final String INVALID_VALUE_FOR = "Invalid value %s for %s. Available values: %s";
}
