package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String UNKNOWN_ERROR = "Unknown Error";
    public static final String API_V1 = "/api/v1";
    public static final String PLATFORMS_ENDPOINT = API_V1 + "/platforms";
    public static final String USER_CRYPTOS_ENDPOINT = API_V1 + "/cryptos";

}