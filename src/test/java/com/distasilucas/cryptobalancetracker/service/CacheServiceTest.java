package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.SimpleKey;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalResponse;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static com.distasilucas.cryptobalancetracker.constants.Constants.ALL_PLATFORMS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.GOAL_RESPONSE_GOAL_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PAGE_GOALS_RESPONSE_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_PLATFORMS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_RESPONSE_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTO_ID_CACHE;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CacheServiceTest {

    @Mock
    private CacheManager cacheManagerMock;

    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        cacheService = new CacheService(cacheManagerMock);
    }

    @Test
    void shouldInvalidateUserCryptosCacheIfItExists() {
        var userCrypto = getUserCrypto();

        var userCryptosCacheMap = Map.of(SimpleKey.class, List.of(userCrypto));
        var userCryptosPlatformIdMap = Map.of("123e4567-e89b-12d3-a456-426614174111", List.of(userCrypto));
        var userCryptosCoingeckoCryptoIdMap = Map.of("bitcoin", List.of(userCrypto));
        var userCryptoIdMap = Map.of("bc7a8ee5-13f9-4405-a7fb-887458c21bed", List.of(userCrypto));
        var userCryptosPageMap = Map.of(0, List.of(userCrypto));

        var userCryptosCacheCache = new ConcurrentMapCache(USER_CRYPTOS_CACHE, new ConcurrentHashMap<>(userCryptosCacheMap), false);
        var userCryptosPlatformIdCache = new ConcurrentMapCache(USER_CRYPTOS_PLATFORM_ID_CACHE, new ConcurrentHashMap<>(userCryptosPlatformIdMap), false);
        var userCryptosCoingeckoCryptoIdCache = new ConcurrentMapCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, new ConcurrentHashMap<>(userCryptosCoingeckoCryptoIdMap), false);
        var userCryptoIdCache = new ConcurrentMapCache(USER_CRYPTO_ID_CACHE, new ConcurrentHashMap<>(userCryptoIdMap), false);
        var userCryptosPageCache = new ConcurrentMapCache(USER_CRYPTOS_PAGE_CACHE, new ConcurrentHashMap<>(userCryptosPageMap), false);

        when(cacheManagerMock.getCache(USER_CRYPTOS_CACHE)).thenReturn(userCryptosCacheCache);
        when(cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE)).thenReturn(userCryptosPlatformIdCache);
        when(cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE)).thenReturn(userCryptosCoingeckoCryptoIdCache);
        when(cacheManagerMock.getCache(USER_CRYPTO_ID_CACHE)).thenReturn(userCryptoIdCache);
        when(cacheManagerMock.getCache(USER_CRYPTOS_PAGE_CACHE)).thenReturn(userCryptosPageCache);

        cacheService.invalidateUserCryptosCaches();

        var userCryptosCacheStore = userCryptosCacheCache.getNativeCache();
        var userCryptosPlatformIdStore = userCryptosPlatformIdCache.getNativeCache();
        var userCryptosCoingeckoCryptoIdStore = userCryptosCoingeckoCryptoIdCache.getNativeCache();
        var userCryptoIdStore = userCryptoIdCache.getNativeCache();
        var userCryptosPageStore = userCryptosPageCache.getNativeCache();

        assertTrue(userCryptosCacheStore.isEmpty());
        assertTrue(userCryptosPlatformIdStore.isEmpty());
        assertTrue(userCryptosCoingeckoCryptoIdStore.isEmpty());
        assertTrue(userCryptoIdStore.isEmpty());
        assertTrue(userCryptosPageStore.isEmpty());
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_PLATFORM_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTO_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_PAGE_CACHE);
    }

    @Test
    void shouldThrowNullPointerExceptionIfUserCryptosCachesDontExists() {
        when(cacheManagerMock.getCache(USER_CRYPTOS_CACHE)).thenReturn(null);
        when(cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE)).thenReturn(null);
        when(cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE)).thenReturn(null);

        assertThrows(
            NullPointerException.class,
            () -> cacheService.invalidateUserCryptosCaches()
        );
    }

    @Test
    void shouldInvalidatePlatformsCacheIfItExists() {
        var platform = new Platform("123e4567-e89b-12d3-a456-426614174000", "BINANCE");
        var platformsIdsMap = Map.of(List.of("123e4567-e89b-12d3-a456-426614174000"), List.of(platform));
        var allPlatformsMap = Map.of(SimpleKey.class, List.of(platform));
        var platformIdMap = Map.of("123e4567-e89b-12d3-a456-426614174000", platform);

        var platformsIdsCache = new ConcurrentMapCache(PLATFORMS_PLATFORMS_IDS_CACHE, new ConcurrentHashMap<>(platformsIdsMap), false);
        var allPlatformsCache = new ConcurrentMapCache(ALL_PLATFORMS_CACHE, new ConcurrentHashMap<>(allPlatformsMap), false);
        var platformIdCache = new ConcurrentMapCache(PLATFORM_PLATFORM_ID_CACHE, new ConcurrentHashMap<>(platformIdMap), false);

        when(cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE)).thenReturn(platformsIdsCache);
        when(cacheManagerMock.getCache(ALL_PLATFORMS_CACHE)).thenReturn(allPlatformsCache);
        when(cacheManagerMock.getCache(PLATFORM_PLATFORM_ID_CACHE)).thenReturn(platformIdCache);

        cacheService.invalidatePlatformsCaches();

        var platformsIdsStore = platformsIdsCache.getNativeCache();
        var allPlatformsStore = allPlatformsCache.getNativeCache();
        var platformIdStore = platformIdCache.getNativeCache();

        assertTrue(platformsIdsStore.isEmpty());
        assertTrue(allPlatformsStore.isEmpty());
        assertTrue(platformIdStore.isEmpty());
        verify(cacheManagerMock, times(1)).getCache(PLATFORMS_PLATFORMS_IDS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(ALL_PLATFORMS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PLATFORM_PLATFORM_ID_CACHE);
    }

    @Test
    void shouldThrowNullPointerExceptionIfPlatformsCacheDoesNotExists() {
        when(cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE)).thenReturn(null);

        assertThrows(
            NullPointerException.class,
            () -> cacheService.invalidatePlatformsCaches()
        );
    }

    @Test
    void shouldInvalidateCryptosCacheIfItExists() {
        var crypto = getBitcoinCryptoEntity();
        var map = Map.of(List.of("bitcoin"), List.of(crypto));
        var cache = new ConcurrentMapCache(CRYPTOS_CRYPTOS_IDS_CACHE, new ConcurrentHashMap<>(map), false);

        when(cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE)).thenReturn(cache);

        cacheService.invalidateCryptosCache();

        var store = cache.getNativeCache();

        assertTrue(store.isEmpty());
        verify(cacheManagerMock, times(1)).getCache(CRYPTOS_CRYPTOS_IDS_CACHE);
    }

    @Test
    void shouldThrowNullPointerExceptionIfCryptosCacheDoesNotExists() {
        when(cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE)).thenReturn(null);

        assertThrows(
            NullPointerException.class,
            () -> cacheService.invalidateCryptosCache()
        );
    }

    @Test
    void shouldInvalidateGoalsCacheIfItExists() {
        var goalResponseGoalIdMap = Map.of("123e4567-e89b-12d3-a456-426614174111", getGoalResponse());
        var pageGoalsResponsePageMap = Map.of(0, new PageGoalResponse(0, 1, List.of(getGoalResponse())));

        var goalResponseGoalIdCache = new ConcurrentMapCache(GOAL_RESPONSE_GOAL_ID_CACHE, new ConcurrentHashMap<>(goalResponseGoalIdMap), false);
        var pageGoalsResponsePageCache = new ConcurrentMapCache(PAGE_GOALS_RESPONSE_PAGE_CACHE, new ConcurrentHashMap<>(pageGoalsResponsePageMap), false);

        when(cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE)).thenReturn(goalResponseGoalIdCache);
        when(cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE)).thenReturn(pageGoalsResponsePageCache);

        cacheService.invalidateGoalsCaches();

        var goalResponseGoalIdStore = goalResponseGoalIdCache.getNativeCache();
        var pageGoalsResponsePageStore = pageGoalsResponsePageCache.getNativeCache();

        assertTrue(goalResponseGoalIdStore.isEmpty());
        assertTrue(pageGoalsResponsePageStore.isEmpty());
        verify(cacheManagerMock, times(1)).getCache(GOAL_RESPONSE_GOAL_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE);
    }

    @Test
    void  shouldThrowNullPointerExceptionIfGoalsCacheDoesNotExists() {
        when(cacheManagerMock.getCache(GOAL_RESPONSE_GOAL_ID_CACHE)).thenReturn(null);
        when(cacheManagerMock.getCache(PAGE_GOALS_RESPONSE_PAGE_CACHE)).thenReturn(null);

        assertThrows(
            NullPointerException.class,
            () -> cacheService.invalidateGoalsCaches()
        );
    }

    @Test
    void  shouldInvalidatePriceTargetsCacheIfItExists() {
        var priceTarget = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity());
        var priceTargetResponse = new PriceTargetResponse(
            "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
            "Bitcoin",
            "58000",
            "120000",
            50F
        );
        var priceTargetIdMap = Map.of("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTarget);
        var priceTargetResponseIdMap = Map.of("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetResponse);
        var priceTargetResponsePageMap = Map.of(0, new PagePriceTargetResponse(0, 1, List.of(priceTargetResponse)));

        var priceTargetIdCache = new ConcurrentMapCache(PRICE_TARGET_ID_CACHE, new ConcurrentHashMap<>(priceTargetIdMap), false);
        var priceTargetResponseIdCache = new ConcurrentMapCache(PRICE_TARGET_RESPONSE_ID_CACHE, new ConcurrentHashMap<>(priceTargetResponseIdMap), false);
        var priceTargetResponsePageCache = new ConcurrentMapCache(PRICE_TARGET_PAGE_CACHE, new ConcurrentHashMap<>(priceTargetResponsePageMap), false);

        when(cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE)).thenReturn(priceTargetIdCache);
        when(cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE)).thenReturn(priceTargetResponseIdCache);
        when(cacheManagerMock.getCache(PRICE_TARGET_PAGE_CACHE)).thenReturn(priceTargetResponsePageCache);

        cacheService.invalidatePriceTargetCaches();

        var priceTargetIdStore = priceTargetIdCache.getNativeCache();
        var priceTargetResponseIdStore = priceTargetResponseIdCache.getNativeCache();
        var priceTargetResponsePageStore = priceTargetResponsePageCache.getNativeCache();

        assertTrue(priceTargetIdStore.isEmpty());
        assertTrue(priceTargetResponseIdStore.isEmpty());
        assertTrue(priceTargetResponsePageStore.isEmpty());
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_RESPONSE_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_PAGE_CACHE);
    }

    @Test
    void  shouldThrowNullPointerExceptionIfPriceTargetsCacheDoesNotExists() {
        when(cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE)).thenReturn(null);
        when(cacheManagerMock.getCache(PRICE_TARGET_RESPONSE_ID_CACHE)).thenReturn(null);
        when(cacheManagerMock.getCache(PRICE_TARGET_PAGE_CACHE)).thenReturn(null);

        assertThrows(
            NullPointerException.class,
            () -> cacheService.invalidatePriceTargetCaches()
        );
    }

}
