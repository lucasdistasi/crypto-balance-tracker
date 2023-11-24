package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationConstants {

    public static final String PLATFORM_NAME_REGEX = "^[a-zA-Z]{1,24}$";
    public static final String CRYPTO_NAME_REGEX = "^(?! )(?!.* {2})[a-zA-Z0-9]{1,64}(?: [a-zA-Z0-9]{1,64})*$(?<! )";
    public static final String NULL_BLANK_PLATFORM_NAME = "Platform name cannot be null or blank";
    public static final String INVALID_PLATFORM_NAME = "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed";
    public static final String PLATFORM_ID_UUID = "Platform id must be a valid UUID";
    public static final String CRYPTO_NAME_NOT_BLANK = "Crypto name can not be null or blank";
    public static final String CRYPTO_NAME_SIZE = "Crypto name must be between 1 and 64 characters";
    public static final String CRYPTO_QUANTITY_NOT_NULL = "Crypto quantity can not be null";
    public static final String CRYPTO_QUANTITY_DIGITS = "Crypto quantity must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part";
    public static final String CRYPTO_QUANTITY_DECIMAL_MAX = "Crypto quantity must be less than or equal to 9999999999999999.999999999999";
    public static final String CRYPTO_QUANTITY_POSITIVE = "Crypto quantity must be greater than 0";
    public static final String PLATFORM_ID_NOT_BLANK = "Platform id can not be null or blank";
    public static final String INVALID_PAGE_NUMBER = "Page must be greater than or equal to 0";
    public static final String USER_CRYPTO_ID_UUID = "User crypto id must be a valid UUID";
}
