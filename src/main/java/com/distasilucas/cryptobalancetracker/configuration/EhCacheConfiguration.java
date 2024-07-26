package com.distasilucas.cryptobalancetracker.configuration;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;

import javax.cache.CacheManager;
import javax.cache.Caching;
import java.time.Duration;
import java.util.Collection;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.ALL_PLATFORMS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.COINGECKO_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_BALANCES_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_INFO_CACHE;
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
import static org.springframework.data.util.CastUtils.cast;

@Configuration
public class EhCacheConfiguration {

    @Bean
    public CacheManager ehcacheManager() {
        var cacheManager = Caching.getCachingProvider().getCacheManager();
        Class<List<CoingeckoCrypto>> coingeckoCryptoList = cast(List.class);
        Class<List<UserCrypto>> userCryptoList = cast(List.class);
        Class<Page<UserCrypto>> userCryptoPage = cast(Page.class);
        Class<Collection<String>> stringCollection = cast(Collection.class);
        Class<List<Platform>> platformList = cast(List.class);
        Class<List<Crypto>> cryptoList = cast(List.class);
        Class<Page<PriceTarget>> priceTargetPage = cast(Page.class);
        Class<Page<Goal>> goalPage = cast(Page.class);

        var coingeckoCryptosCache = getCacheConfig(SimpleKey.class, coingeckoCryptoList, Duration.ofDays(3));
        var coingeckoCryptoInfoCache = getCacheConfig(String.class, CoingeckoCryptoInfo.class, Duration.ofMinutes(10));
        var userCryptosCache = getCacheConfig(SimpleKey.class, userCryptoList);
        var userCryptosPlatformIdCache = getCacheConfig(String.class, userCryptoList);
        var userCryptosCoingeckoCryptoIdCache = getCacheConfig(String.class, userCryptoList);
        var userCryptoIdCache = getCacheConfig(String.class, UserCrypto.class);
        var userCryptosPageCache = getCacheConfig(Integer.class, userCryptoPage);
        var platformsIdsCache = getCacheConfig(stringCollection, platformList);
        var cryptoCoingeckoCryptoIdCache = getCacheConfig(String.class, Crypto.class, Duration.ofMinutes(2));
        var cryptosIdsCache = getCacheConfig(stringCollection, cryptoList, Duration.ofMinutes(2));
        var allPlatformsCache = getCacheConfig(SimpleKey.class, platformList, Duration.ofDays(10));
        var platformCache = getCacheConfig(String.class, Platform.class, Duration.ofDays(10));
        var priceTargetCache = getCacheConfig(String.class, PriceTarget.class);
        var pagePriceTargetCache = getCacheConfig(Integer.class, priceTargetPage);
        var goalCache = getCacheConfig(String.class, Goal.class);
        var pageGoalsCache = getCacheConfig(Integer.class, goalPage);
        var totalBalancesCache = getCacheConfig(SimpleKey.class, BalancesResponse.class, Duration.ofMinutes(5));
        var datesBalancesCache = getCacheConfig(DateRange.class, DatesBalanceResponse.class, Duration.ofMinutes(5));
        var platformInsightsCache = getCacheConfig(String.class, PlatformInsightsResponse.class, Duration.ofMinutes(5));
        var cryptoInsightsCache = getCacheConfig(String.class, CryptoInsightResponse.class, Duration.ofMinutes(5));
        var platformsBalancesInsightsCache = getCacheConfig(SimpleKey.class, PlatformsBalancesInsightsResponse.class, Duration.ofMinutes(5));
        var cryptosBalancesInsightsCache = getCacheConfig(SimpleKey.class, CryptosBalancesInsightsResponse.class, Duration.ofMinutes(5));

        cacheManager.createCache(COINGECKO_CRYPTOS_CACHE, coingeckoCryptosCache);
        cacheManager.createCache(CRYPTO_INFO_CACHE, coingeckoCryptoInfoCache);
        cacheManager.createCache(USER_CRYPTOS_CACHE, userCryptosCache);
        cacheManager.createCache(USER_CRYPTOS_PLATFORM_ID_CACHE, userCryptosPlatformIdCache);
        cacheManager.createCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, userCryptosCoingeckoCryptoIdCache);
        cacheManager.createCache(USER_CRYPTO_ID_CACHE, userCryptoIdCache);
        cacheManager.createCache(USER_CRYPTOS_PAGE_CACHE, userCryptosPageCache);
        cacheManager.createCache(PLATFORMS_PLATFORMS_IDS_CACHE, platformsIdsCache);
        cacheManager.createCache(CRYPTO_COINGECKO_CRYPTO_ID_CACHE, cryptoCoingeckoCryptoIdCache);
        cacheManager.createCache(CRYPTOS_CRYPTOS_IDS_CACHE, cryptosIdsCache);
        cacheManager.createCache(ALL_PLATFORMS_CACHE, allPlatformsCache);
        cacheManager.createCache(PLATFORM_PLATFORM_ID_CACHE, platformCache);
        cacheManager.createCache(PRICE_TARGET_ID_CACHE, priceTargetCache);
        cacheManager.createCache(PRICE_TARGET_PAGE_CACHE, pagePriceTargetCache);
        cacheManager.createCache(GOAL_CACHE, goalCache);
        cacheManager.createCache(PAGE_GOALS_CACHE, pageGoalsCache);
        cacheManager.createCache(TOTAL_BALANCES_CACHE, totalBalancesCache);
        cacheManager.createCache(DATES_BALANCES_CACHE, datesBalancesCache);
        cacheManager.createCache(PLATFORM_INSIGHTS_CACHE, platformInsightsCache);
        cacheManager.createCache(CRYPTO_INSIGHTS_CACHE, cryptoInsightsCache);
        cacheManager.createCache(PLATFORMS_BALANCES_INSIGHTS_CACHE, platformsBalancesInsightsCache);
        cacheManager.createCache(CRYPTOS_BALANCES_INSIGHTS_CACHE, cryptosBalancesInsightsCache);

        return cacheManager;
    }

    private <K,V> javax.cache.configuration.Configuration<K, V> getCacheConfig(
        Class<K> key,
        Class<V> value,
        ResourcePoolsBuilder resourcePoolsBuilder,
        Duration duration
    ) {
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(duration);
        var cache = CacheConfigurationBuilder.newCacheConfigurationBuilder(
            key,
            value,
            resourcePoolsBuilder
        ).withExpiry(expirationPolicyBuilder).build();

        return Eh107Configuration.fromEhcacheCacheConfiguration(cache);
    }

    private <K,V> javax.cache.configuration.Configuration<K, V> getCacheConfig(
        Class<K> key,
        Class<V> value
    ) {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.MB);

        return getCacheConfig(key, value, resourcePools, Duration.ofMinutes(60));
    }

    private <K,V> javax.cache.configuration.Configuration<K, V> getCacheConfig(
        Class<K> key,
        Class<V> value,
        Duration duration
    ) {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.MB);

        return getCacheConfig(key, value, resourcePools, duration);
    }
}
