package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights;
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
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.model.CacheType.CRYPTOS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.GOALS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.INSIGHTS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.PLATFORMS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.PRICE_TARGETS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.USER_CRYPTOS_CACHES;
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
    void shouldInvalidateUserCryptosCaches() {
        var userCrypto = getUserCrypto();

        var userCryptosCacheMap = Map.of(SimpleKey.class, List.of(userCrypto));
        var userCryptosPlatformIdMap = Map.of("123e4567-e89b-12d3-a456-426614174111", List.of(userCrypto));
        var userCryptosCoingeckoCryptoIdMap = Map.of("bitcoin", List.of(userCrypto));
        var userCryptoIdMap = Map.of("bc7a8ee5-13f9-4405-a7fb-887458c21bed", List.of(userCrypto));
        var userCryptosPageMap = Map.of(0, List.of(userCrypto));

        var userCryptosCacheCache = getMapCache(USER_CRYPTOS_CACHE, userCryptosCacheMap);
        var userCryptosPlatformIdCache = getMapCache(USER_CRYPTOS_PLATFORM_ID_CACHE, userCryptosPlatformIdMap);
        var userCryptosCoingeckoCryptoIdCache = getMapCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, userCryptosCoingeckoCryptoIdMap);
        var userCryptoIdCache = getMapCache(USER_CRYPTO_ID_CACHE, userCryptoIdMap);
        var userCryptosPageCache = getMapCache(USER_CRYPTOS_PAGE_CACHE, userCryptosPageMap);

        when(cacheManagerMock.getCache(USER_CRYPTOS_CACHE)).thenReturn(userCryptosCacheCache);
        when(cacheManagerMock.getCache(USER_CRYPTOS_PLATFORM_ID_CACHE)).thenReturn(userCryptosPlatformIdCache);
        when(cacheManagerMock.getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE)).thenReturn(userCryptosCoingeckoCryptoIdCache);
        when(cacheManagerMock.getCache(USER_CRYPTO_ID_CACHE)).thenReturn(userCryptoIdCache);
        when(cacheManagerMock.getCache(USER_CRYPTOS_PAGE_CACHE)).thenReturn(userCryptosPageCache);

        cacheService.invalidate(USER_CRYPTOS_CACHES);

        assertTrue(userCryptosCacheCache.getNativeCache().isEmpty());
        assertTrue(userCryptosPlatformIdCache.getNativeCache().isEmpty());
        assertTrue(userCryptosCoingeckoCryptoIdCache.getNativeCache().isEmpty());
        assertTrue(userCryptoIdCache.getNativeCache().isEmpty());
        assertTrue(userCryptosPageCache.getNativeCache().isEmpty());

        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_PLATFORM_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTO_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(USER_CRYPTOS_PAGE_CACHE);
    }

    @Test
    void shouldInvalidateCryptosCaches() {
        var crypto = getBitcoinCryptoEntity();
        var map = Map.of(List.of("bitcoin"), List.of(crypto));
        var cache = getMapCache(CRYPTOS_CRYPTOS_IDS_CACHE, map);

        when(cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE)).thenReturn(cache);

        cacheService.invalidate(CRYPTOS_CACHES);

        assertTrue(cache.getNativeCache().isEmpty());
        verify(cacheManagerMock, times(1)).getCache(CRYPTOS_CRYPTOS_IDS_CACHE);
    }

    @Test
    void shouldInvalidatePlatformsCaches() {
        var platform = new Platform("123e4567-e89b-12d3-a456-426614174000", "BINANCE");
        var platformsIdsMap = Map.of(List.of("123e4567-e89b-12d3-a456-426614174000"), List.of(platform));
        var allPlatformsMap = Map.of(SimpleKey.class, List.of(platform));
        var platformIdMap = Map.of("123e4567-e89b-12d3-a456-426614174000", platform);

        var platformsIdsCache = getMapCache(PLATFORMS_PLATFORMS_IDS_CACHE, platformsIdsMap);
        var allPlatformsCache = getMapCache(ALL_PLATFORMS_CACHE, allPlatformsMap);
        var platformIdCache = getMapCache(PLATFORM_PLATFORM_ID_CACHE, platformIdMap);

        when(cacheManagerMock.getCache(PLATFORMS_PLATFORMS_IDS_CACHE)).thenReturn(platformsIdsCache);
        when(cacheManagerMock.getCache(ALL_PLATFORMS_CACHE)).thenReturn(allPlatformsCache);
        when(cacheManagerMock.getCache(PLATFORM_PLATFORM_ID_CACHE)).thenReturn(platformIdCache);

        cacheService.invalidate(PLATFORMS_CACHES);

        assertTrue(platformsIdsCache.getNativeCache().isEmpty());
        assertTrue(allPlatformsCache.getNativeCache().isEmpty());
        assertTrue(platformIdCache.getNativeCache().isEmpty());
        verify(cacheManagerMock, times(1)).getCache(PLATFORMS_PLATFORMS_IDS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(ALL_PLATFORMS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PLATFORM_PLATFORM_ID_CACHE);
    }

    @Test
    void shouldInvalidateGoalsCaches() {
        var goalResponseGoalIdMap = Map.of("123e4567-e89b-12d3-a456-426614174111", getGoalResponse());
        var pageGoalsResponsePageMap = Map.of(0, new PageGoalResponse(0, 1, List.of(getGoalResponse())));

        var goalResponseGoalIdCache = getMapCache(GOAL_CACHE, goalResponseGoalIdMap);
        var pageGoalsResponsePageCache = getMapCache(PAGE_GOALS_CACHE, pageGoalsResponsePageMap);

        when(cacheManagerMock.getCache(GOAL_CACHE)).thenReturn(goalResponseGoalIdCache);
        when(cacheManagerMock.getCache(PAGE_GOALS_CACHE)).thenReturn(pageGoalsResponsePageCache);

        cacheService.invalidate(GOALS_CACHES);

        assertTrue(goalResponseGoalIdCache.getNativeCache().isEmpty());
        assertTrue(pageGoalsResponsePageCache.getNativeCache().isEmpty());
        verify(cacheManagerMock, times(1)).getCache(GOAL_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PAGE_GOALS_CACHE);
    }

    @Test
    void  shouldInvalidatePriceTargetsCaches() {
        var priceTarget = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity());
        var priceTargetResponse = new PriceTargetResponse(
            "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
            "Bitcoin",
            "58000",
            "120000",
            50F
        );
        var priceTargetIdMap = Map.of("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTarget);
        var priceTargetResponsePageMap = Map.of(0, new PagePriceTargetResponse(0, 1, List.of(priceTargetResponse)));

        var priceTargetIdCache = getMapCache(PRICE_TARGET_ID_CACHE, priceTargetIdMap);
        var priceTargetResponsePageCache = getMapCache(PRICE_TARGET_PAGE_CACHE, priceTargetResponsePageMap);

        when(cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE)).thenReturn(priceTargetIdCache);
        when(cacheManagerMock.getCache(PRICE_TARGET_PAGE_CACHE)).thenReturn(priceTargetResponsePageCache);

        cacheService.invalidate(PRICE_TARGETS_CACHES);

        assertTrue(priceTargetIdCache.getNativeCache().isEmpty());
        assertTrue(priceTargetResponsePageCache.getNativeCache().isEmpty());
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_PAGE_CACHE);
    }

    @Test
    void  shouldInvalidateInsightsCaches() {
        var totalBalancesMap = Map.of(SimpleKey.class, new BalancesResponse("1000", "927.30", "0.015384615"));
        var datesBalancesMap = Map.of(DateRange.class, getDateBalanceResponse());
        var platformInsightsMap = Map.of(String.class, getPlatformInsightsResponse());
        var cryptoInsightsMap = Map.of(String.class, getCryptoInsightResponse());
        var platformsBalancesInsightsMap = Map.of(SimpleKey.class, getPlatformsBalancesInsightsResponse());
        var cryptosBalancesInsightsMap = Map.of(SimpleKey.class, getCryptosBalancesInsightsResponse());

        var totalBalancesCache = getMapCache(TOTAL_BALANCES_CACHE, totalBalancesMap);
        var datesBalancesCache = getMapCache(DATES_BALANCES_CACHE, datesBalancesMap);
        var platformInsightsCache = getMapCache(PLATFORM_INSIGHTS_CACHE, platformInsightsMap);
        var cryptoInsightsCache = getMapCache(CRYPTO_INSIGHTS_CACHE, cryptoInsightsMap);
        var platformsBalancesInsightsCache = getMapCache(PLATFORMS_BALANCES_INSIGHTS_CACHE, platformsBalancesInsightsMap);
        var cryptosBalancesInsightsCache = getMapCache(CRYPTOS_BALANCES_INSIGHTS_CACHE, cryptosBalancesInsightsMap);

        when(cacheManagerMock.getCache(TOTAL_BALANCES_CACHE)).thenReturn(totalBalancesCache);
        when(cacheManagerMock.getCache(DATES_BALANCES_CACHE)).thenReturn(datesBalancesCache);
        when(cacheManagerMock.getCache(PLATFORM_INSIGHTS_CACHE)).thenReturn(platformInsightsCache);
        when(cacheManagerMock.getCache(CRYPTO_INSIGHTS_CACHE)).thenReturn(cryptoInsightsCache);
        when(cacheManagerMock.getCache(PLATFORMS_BALANCES_INSIGHTS_CACHE)).thenReturn(platformsBalancesInsightsCache);
        when(cacheManagerMock.getCache(CRYPTOS_BALANCES_INSIGHTS_CACHE)).thenReturn(cryptosBalancesInsightsCache);

        cacheService.invalidate(INSIGHTS_CACHES);

        assertTrue(totalBalancesCache.getNativeCache().isEmpty());
        assertTrue(datesBalancesCache.getNativeCache().isEmpty());
        assertTrue(platformInsightsCache.getNativeCache().isEmpty());
        assertTrue(cryptoInsightsCache.getNativeCache().isEmpty());
        assertTrue(platformsBalancesInsightsCache.getNativeCache().isEmpty());
        assertTrue(cryptosBalancesInsightsCache.getNativeCache().isEmpty());

        verify(cacheManagerMock, times(1)).getCache(TOTAL_BALANCES_CACHE);
        verify(cacheManagerMock, times(1)).getCache(DATES_BALANCES_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PLATFORM_INSIGHTS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(CRYPTO_INSIGHTS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PLATFORMS_BALANCES_INSIGHTS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(CRYPTOS_BALANCES_INSIGHTS_CACHE);
    }

    @Test
    void shouldInvalidateCaches() {
        var crypto = getBitcoinCryptoEntity();
        var priceTarget = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity());
        var priceTargetResponse = new PriceTargetResponse(
            "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
            "Bitcoin",
            "58000",
            "120000",
            50F
        );

        var cryptosIdsMap = Map.of(List.of("bitcoin"), List.of(crypto));
        var priceTargetIdMap = Map.of("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTarget);
        var priceTargetResponsePageMap = Map.of(0, new PagePriceTargetResponse(0, 1, List.of(priceTargetResponse)));

        var cryptosIdsCache = getMapCache(CRYPTOS_CRYPTOS_IDS_CACHE, cryptosIdsMap);
        var priceTargetIdCache = getMapCache(PRICE_TARGET_ID_CACHE, priceTargetIdMap);
        var priceTargetResponsePageCache = getMapCache(PRICE_TARGET_PAGE_CACHE, priceTargetResponsePageMap);


        when(cacheManagerMock.getCache(CRYPTOS_CRYPTOS_IDS_CACHE)).thenReturn(cryptosIdsCache);
        when(cacheManagerMock.getCache(PRICE_TARGET_ID_CACHE)).thenReturn(priceTargetIdCache);
        when(cacheManagerMock.getCache(PRICE_TARGET_PAGE_CACHE)).thenReturn(priceTargetResponsePageCache);

        cacheService.invalidate(CRYPTOS_CACHES, PRICE_TARGETS_CACHES);

        assertTrue(cryptosIdsCache.getNativeCache().isEmpty());
        assertTrue(priceTargetIdCache.getNativeCache().isEmpty());
        assertTrue(priceTargetResponsePageCache.getNativeCache().isEmpty());

        verify(cacheManagerMock, times(1)).getCache(CRYPTOS_CRYPTOS_IDS_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_ID_CACHE);
        verify(cacheManagerMock, times(1)).getCache(PRICE_TARGET_PAGE_CACHE);
    }

    private ConcurrentMapCache getMapCache(String name, Map<?, ?> map) {
        return new ConcurrentMapCache(name, new ConcurrentHashMap<>(map), false);
    }

    private PlatformInsightsResponse getPlatformInsightsResponse() {
        return new PlatformInsightsResponse(
            "BINANCE",
            new BalancesResponse("7500.00", "0.25", "6750.00"),
            List.of(
                new CryptoInsights(
                    "123e4567-e89b-12d3-a456-426614174000",
                    "Bitcoin",
                    "bitcoin",
                    "0.25",
                    new BalancesResponse("7500.00", "0.25", "6750.00"),
                    100f
                )
            )
        );
    }

    private DatesBalanceResponse getDateBalanceResponse() {
        return new DatesBalanceResponse(
            List.of(
                new DateBalances("16 March 2024", new BalancesResponse("1000", "918.45", "0.01438911")),
                new DateBalances("17 March 2024", new BalancesResponse("1500", "1377.67", "0.021583665"))
            ),
            new BalanceChanges(50F, 50F, 49.99F),
            new DifferencesChanges("500", "459.22", "0.007194555")
        );
    }

    private CryptosBalancesInsightsResponse getCryptosBalancesInsightsResponse() {
        return new CryptosBalancesInsightsResponse(
            new BalancesResponse("7108.39", "0.2512793593", "6484.23"),
            List.of(
                new CryptoInsights(
                    "Bitcoin",
                    "bitcoin",
                    "0.15",
                    new BalancesResponse("4500.00", "0.15", "4050.00"),
                    63.31f
                ),
                new CryptoInsights(
                    "Ethereum",
                    "ethereum",
                    "1.372",
                    new BalancesResponse("2219.13", "0.0861664843", "2070.86"),
                     31.22f
                ),
                new CryptoInsights(
                    "Tether",
                    "tether",
                    "200",
                    new BalancesResponse("199.92", "0.00776", "186.62"),
                    2.81f
                ),
                new CryptoInsights(
                    "Litecoin",
                    "litecoin",
                    "3.125",
                    new BalancesResponse("189.34", "0.007352875", "176.75"),
                    2.66f
                )
            )
        );
    }

    private PlatformsBalancesInsightsResponse getPlatformsBalancesInsightsResponse() {
        return new PlatformsBalancesInsightsResponse(
            new BalancesResponse("7108.39", "0.2512793593", "6484.23"),
            List.of(
                new PlatformsInsights(
                    "BINANCE",
                    new BalancesResponse("5120.45", "0.1740889256", "4629.06"),
                    72.03f
                ),
                new PlatformsInsights(
                    "COINBASE",
                    new BalancesResponse("1987.93", "0.0771904337", "1855.17"),
                    27.97f
                )
            )
        );
    }

    private CryptoInsightResponse getCryptoInsightResponse() {
        return new CryptoInsightResponse(
            "Bitcoin",
            new BalancesResponse("7500.00", "0.25", "6750.00"),
            List.of(
                new PlatformInsight(
                    "0.25",
                    new BalancesResponse("7500.00", "0.25", "6750.00"),
                    100f,
                    "BINANCE"
                )
            )
        );
    }
}
