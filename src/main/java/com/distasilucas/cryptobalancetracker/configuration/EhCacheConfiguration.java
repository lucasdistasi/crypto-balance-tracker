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
import java.util.Map;

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
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTO_ID_CACHE;
import static org.springframework.data.util.CastUtils.cast;

@Configuration
public class EhCacheConfiguration {

    @Bean
    public CacheManager ehcacheManager() {
        var cacheManager = Caching.getCachingProvider().getCacheManager();
        getAllCaches().forEach(cacheManager::createCache);

        return cacheManager;
    }

    private Map<String, javax.cache.configuration.Configuration<?, ?>> getAllCaches() {
        Class<List<CoingeckoCrypto>> coingeckoCryptoList = cast(List.class);
        Class<List<UserCrypto>> userCryptoList = cast(List.class);
        Class<Page<UserCrypto>> userCryptoPage = cast(Page.class);
        Class<Collection<String>> stringCollection = cast(Collection.class);
        Class<List<Platform>> platformList = cast(List.class);
        Class<List<Crypto>> cryptoList = cast(List.class);
        Class<Page<PriceTarget>> priceTargetPage = cast(Page.class);
        Class<Page<Goal>> goalPage = cast(Page.class);

        return Map.ofEntries(
            Map.entry(COINGECKO_CRYPTOS_CACHE, getCacheConfig(SimpleKey.class, coingeckoCryptoList, Duration.ofDays(3))),
            Map.entry(CRYPTO_INFO_CACHE, getCacheConfig(String.class, CoingeckoCryptoInfo.class, Duration.ofMinutes(10))),
            Map.entry(USER_CRYPTOS_CACHE, getCacheConfig(SimpleKey.class, userCryptoList)),
            Map.entry(USER_CRYPTOS_PLATFORM_ID_CACHE, getCacheConfig(String.class, userCryptoList)),
            Map.entry(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, getCacheConfig(String.class, userCryptoList)),
            Map.entry(USER_CRYPTO_ID_CACHE, getCacheConfig(String.class, UserCrypto.class)),
            Map.entry(USER_CRYPTOS_PAGE_CACHE, getCacheConfig(Integer.class, userCryptoPage)),
            Map.entry(PLATFORMS_PLATFORMS_IDS_CACHE, getCacheConfig(stringCollection, platformList)),
            Map.entry(CRYPTO_COINGECKO_CRYPTO_ID_CACHE, getCacheConfig(String.class, Crypto.class, Duration.ofMinutes(2))),
            Map.entry(CRYPTOS_CRYPTOS_IDS_CACHE, getCacheConfig(stringCollection, cryptoList, Duration.ofMinutes(2))),
            Map.entry(ALL_PLATFORMS_CACHE, getCacheConfig(SimpleKey.class, platformList, Duration.ofDays(10))),
            Map.entry(PLATFORM_PLATFORM_ID_CACHE, getCacheConfig(String.class, Platform.class, Duration.ofDays(10))),
            Map.entry(PRICE_TARGET_ID_CACHE, getCacheConfig(String.class, PriceTarget.class)),
            Map.entry(PRICE_TARGET_PAGE_CACHE, getCacheConfig(Integer.class, priceTargetPage)),
            Map.entry(GOAL_CACHE, getCacheConfig(String.class, Goal.class)),
            Map.entry(PAGE_GOALS_CACHE, getCacheConfig(Integer.class, goalPage)),
            Map.entry(TOTAL_BALANCES_CACHE, getCacheConfig(SimpleKey.class, BalancesResponse.class, Duration.ofMinutes(5))),
            Map.entry(DATES_BALANCES_CACHE, getCacheConfig(DateRange.class, DatesBalanceResponse.class, Duration.ofMinutes(5))),
            Map.entry(PLATFORM_INSIGHTS_CACHE, getCacheConfig(String.class, PlatformInsightsResponse.class, Duration.ofMinutes(5))),
            Map.entry(CRYPTO_INSIGHTS_CACHE, getCacheConfig(String.class, CryptoInsightResponse.class, Duration.ofMinutes(5))),
            Map.entry(PLATFORMS_BALANCES_INSIGHTS_CACHE, getCacheConfig(SimpleKey.class, PlatformsBalancesInsightsResponse.class, Duration.ofMinutes(5))),
            Map.entry(CRYPTOS_BALANCES_INSIGHTS_CACHE, getCacheConfig(SimpleKey.class, CryptosBalancesInsightsResponse.class, Duration.ofMinutes(5)))
        );
    }

    private <K, V> javax.cache.configuration.Configuration<K, V> getCacheConfig(
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

    private <K, V> javax.cache.configuration.Configuration<K, V> getCacheConfig(
        Class<K> key,
        Class<V> value
    ) {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.MB);

        return getCacheConfig(key, value, resourcePools, Duration.ofMinutes(60));
    }

    private <K, V> javax.cache.configuration.Configuration<K, V> getCacheConfig(
        Class<K> key,
        Class<V> value,
        Duration duration
    ) {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder().offheap(1, MemoryUnit.MB);

        return getCacheConfig(key, value, resourcePools, duration);
    }
}
