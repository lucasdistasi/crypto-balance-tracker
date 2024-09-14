package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.model.CacheType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import static com.distasilucas.cryptobalancetracker.constants.Constants.ALL_PLATFORMS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_BALANCES_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.DATES_BALANCES_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.GOAL_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PAGE_GOALS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_BALANCES_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_PLATFORMS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.TOTAL_BALANCES_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTO_ID_CACHE;

@Slf4j
@Service
@RequiredArgsConstructor
public class CacheService {

    private final CacheManager cacheManager;

    public void invalidate(CacheType firstCache, CacheType ...caches) {
        invalidate(firstCache);

        for (CacheType cache : caches) {
            invalidate(cache);
        }
    }

    private void invalidate(CacheType cache) {
        switch (cache) {
            case USER_CRYPTOS_CACHES -> invalidateUserCryptosCaches();
            case CRYPTOS_CACHES -> invalidateCryptosCache();
            case PLATFORMS_CACHES -> invalidatePlatformsCaches();
            case GOALS_CACHES -> invalidateGoalsCaches();
            case PRICE_TARGETS_CACHES -> invalidatePriceTargetCaches();
            case INSIGHTS_CACHES -> invalidateInsightsCache();
        }
    }

    private void invalidateUserCryptosCaches() {
        log.info("Invalidating user cryptos cache");

        cacheManager.getCache(USER_CRYPTOS_CACHE).invalidate();
        cacheManager.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE).invalidate();
        cacheManager.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE).invalidate();
        cacheManager.getCache(USER_CRYPTO_ID_CACHE).invalidate();
        cacheManager.getCache(USER_CRYPTOS_PAGE_CACHE).invalidate();
    }

    private void invalidatePlatformsCaches() {
        log.info("Invalidating platforms cache");

        cacheManager.getCache(PLATFORMS_PLATFORMS_IDS_CACHE).invalidate();
        cacheManager.getCache(ALL_PLATFORMS_CACHE).invalidate();
        cacheManager.getCache(PLATFORM_PLATFORM_ID_CACHE).invalidate();
    }

    private void invalidateCryptosCache() {
        log.info("Invalidating cryptos cache");

        cacheManager.getCache(CRYPTOS_CRYPTOS_IDS_CACHE).invalidate();
    }

    private void invalidateGoalsCaches() {
        log.info("Invalidating goals cache");

        cacheManager.getCache(GOAL_CACHE).invalidate();
        cacheManager.getCache(PAGE_GOALS_CACHE).invalidate();
    }

    private void invalidatePriceTargetCaches() {
        log.info("Invalidating price target caches");

        cacheManager.getCache(PRICE_TARGET_ID_CACHE).invalidate();
        cacheManager.getCache(PRICE_TARGET_PAGE_CACHE).invalidate();
    }

    private void invalidateInsightsCache() {
        log.info("Invalidating insights caches");

        cacheManager.getCache(TOTAL_BALANCES_CACHE).invalidate();
        cacheManager.getCache(DATES_BALANCES_CACHE).invalidate();
        cacheManager.getCache(PLATFORM_INSIGHTS_CACHE).invalidate();
        cacheManager.getCache(CRYPTO_INSIGHTS_CACHE).invalidate();
        cacheManager.getCache(PLATFORMS_BALANCES_INSIGHTS_CACHE).invalidate();
        cacheManager.getCache(CRYPTOS_BALANCES_INSIGHTS_CACHE).invalidate();
    }
}
