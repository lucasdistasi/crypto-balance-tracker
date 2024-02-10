package com.distasilucas.cryptobalancetracker.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import static com.distasilucas.cryptobalancetracker.constants.Constants.ALL_PLATFORMS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_PLATFORMS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public void invalidateUserCryptosCaches() {
        log.info("Invalidating user cryptos cache");

        cacheManager.getCache(USER_CRYPTOS_CACHE).invalidate();
        cacheManager.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE).invalidate();
        cacheManager.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE).invalidate();
    }

    public void invalidatePlatformsCaches() {
        log.info("Invalidating platforms cache");

        cacheManager.getCache(PLATFORMS_PLATFORMS_IDS_CACHE).invalidate();
        cacheManager.getCache(ALL_PLATFORMS_CACHE).invalidate();
        cacheManager.getCache(PLATFORM_PLATFORM_ID_CACHE).invalidate();
    }

    public void invalidateCryptosCache() {
        log.info("Invalidating cryptos cache");

        cacheManager.getCache(CRYPTOS_CRYPTOS_IDS_CACHE).invalidate();
    }
}
