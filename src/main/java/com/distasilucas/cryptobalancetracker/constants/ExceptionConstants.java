package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionConstants {

    public static final String PLATFORM_ID_NOT_FOUND = "Platform with id %s not found";
    public static final String DUPLICATED_PLATFORM = "Platform %s already exists";
}
