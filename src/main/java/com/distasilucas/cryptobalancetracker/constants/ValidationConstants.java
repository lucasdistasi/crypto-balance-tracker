package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationConstants {

    public static final String PLATFORM_NAME_REGEX = "^[a-zA-Z]{1,24}$";
    public static final String NULL_BLANK_PLATFORM_NAME = "Platform name cannot be null or blank";
    public static final String INVALID_PLATFORM_NAME = "Platform name must be 1-24 characters long, no numbers, special characters or whitespace allowed";
    public static final String PLATFORM_ID_UUID = "Platform id must be a valid UUID";
}
