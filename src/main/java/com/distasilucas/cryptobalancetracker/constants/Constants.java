package com.distasilucas.cryptobalancetracker.constants;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Constants {

    public static final String UNKNOWN_ERROR = "Unknown Error";
    public static final String API_V1 = "/api/v1";
    public static final String PLATFORMS_ENDPOINT = API_V1 + "/platforms";
    public static final String USER_CRYPTOS_ENDPOINT = API_V1 + "/cryptos";
    public static final String GOALS_ENDPOINT = API_V1 + "/goals";
    public static final String INSIGHTS_ENDPOINT = API_V1 + "/insights";
    public static final String PRICE_TARGET_ENDPOINT = API_V1 + "/price-targets";

    public static final String COINGECKO_CRYPTOS_CACHE = "COINGECKO_CRYPTOS_CACHE";
    public static final String CRYPTO_INFO_CACHE = "CRYPTO_INFO_CACHE";
    public static final String USER_CRYPTOS_CACHE = "USER_CRYPTOS_CACHE";
    public static final String USER_CRYPTOS_PLATFORM_ID_CACHE = "USER_CRYPTOS_PLATFORM_ID_CACHE";
    public static final String USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE = "USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE";
    public static final String USER_CRYPTO_ID_CACHE = "USER_CRYPTO_ID_CACHE";
    public static final String USER_CRYPTOS_PAGE_CACHE = "USER_CRYPTOS_PAGE_CACHE";
    public static final String PLATFORMS_PLATFORMS_IDS_CACHE = "PLATFORMS_PLATFORMS_IDS_CACHE";
    public static final String CRYPTO_COINGECKO_CRYPTO_ID_CACHE = "CRYPTO_COINGECKO_CRYPTO_ID_CACHE";
    public static final String CRYPTOS_CRYPTOS_IDS_CACHE = "CRYPTOS_CRYPTOS_IDS_CACHE";
    public static final String ALL_PLATFORMS_CACHE = "ALL_PLATFORMS_CACHE";
    public static final String PLATFORM_PLATFORM_ID_CACHE = "PLATFORM_PLATFORM_ID_CACHE";
    public static final String GOAL_CACHE = "GOAL_CACHE";
    public static final String PAGE_GOALS_CACHE = "PAGE_GOALS_CACHE";
    public static final String PRICE_TARGET_ID_CACHE = "PRICE_TARGET_ID_CACHE";
    public static final String PRICE_TARGET_PAGE_CACHE = "PRICE_TARGET_PAGE_CACHE";
    public static final String TOTAL_BALANCES_CACHE = "TOTAL_BALANCES_CACHE";
    public static final String DATES_BALANCES_CACHE = "DATES_BALANCES_CACHE";
    public static final String PLATFORM_INSIGHTS_CACHE = "PLATFORM_INSIGHTS_CACHE";
    public static final String CRYPTO_INSIGHTS_CACHE = "CRYPTO_INSIGHTS_CACHE";
    public static final String PLATFORMS_BALANCES_INSIGHTS_CACHE = "PLATFORMS_BALANCES_INSIGHTS_CACHE";
    public static final String CRYPTOS_BALANCES_INSIGHTS_CACHE = "CRYPTOS_BALANCES_INSIGHTS_CACHE";

}
