package com.distasilucas.cryptobalancetracker.configuration;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import org.ehcache.config.CacheConfiguration;
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
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_INFO_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.GOAL_RESPONSE_GOAL_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PAGE_GOALS_RESPONSE_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_PLATFORMS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_RESPONSE_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_PAGE_CACHE;
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
        var coingeckoCryptosCache = getCoingeckoCryptosCache();
        var coingeckoCryptoInfoCache = getCoingeckoCryptoInfoCache();
        var userCryptosCache = getAllUserCryptosCache();
        var userCryptosPlatformIdCache = getUserCryptosPlatformIdCache();
        var userCryptosCoingeckoCryptoIdCache = getUserCryptosCoingeckoCryptoIdCache();
        var userCryptoIdCache = getUserCryptoIdCache();
        var userCryptosPageCache = getUserCryptosPageCache();
        var platformsIdsCache = getPlatformsIdsCache();
        var cryptoCoingeckoCryptoIdCache = getCryptoCoingeckoCryptoIdCache();
        var cryptosIdsCache = getCryptosIdsCache();
        var allPlatformsCache = getAllPlatformsCache();
        var platformCache = getPlatformCache();
        var priceTargetCache = getPriceTargetCache();
        var priceTargetResponseCache = getPriceTargetResponseCache();
        var pagePriceTargetCache = getPagePriceTargetCache();
        var goalResponseCache = getGoalResponseCache();
        var pageGoalsResponseCache = getPageGoalsResponseCache();

        cacheManager.createCache(COINGECKO_CRYPTOS_CACHE, getCacheConfiguration(coingeckoCryptosCache));
        cacheManager.createCache(CRYPTO_INFO_CACHE, getCacheConfiguration(coingeckoCryptoInfoCache));
        cacheManager.createCache(USER_CRYPTOS_CACHE, getCacheConfiguration(userCryptosCache));
        cacheManager.createCache(USER_CRYPTOS_PLATFORM_ID_CACHE, getCacheConfiguration(userCryptosPlatformIdCache));
        cacheManager.createCache(USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, getCacheConfiguration(userCryptosCoingeckoCryptoIdCache));
        cacheManager.createCache(USER_CRYPTO_ID_CACHE, getCacheConfiguration(userCryptoIdCache));
        cacheManager.createCache(USER_CRYPTOS_PAGE_CACHE, getCacheConfiguration(userCryptosPageCache));
        cacheManager.createCache(PLATFORMS_PLATFORMS_IDS_CACHE, getCacheConfiguration(platformsIdsCache));
        cacheManager.createCache(CRYPTO_COINGECKO_CRYPTO_ID_CACHE, getCacheConfiguration(cryptoCoingeckoCryptoIdCache));
        cacheManager.createCache(CRYPTOS_CRYPTOS_IDS_CACHE, getCacheConfiguration(cryptosIdsCache));
        cacheManager.createCache(ALL_PLATFORMS_CACHE, getCacheConfiguration(allPlatformsCache));
        cacheManager.createCache(PLATFORM_PLATFORM_ID_CACHE, getCacheConfiguration(platformCache));
        cacheManager.createCache(PRICE_TARGET_ID_CACHE, getCacheConfiguration(priceTargetCache));
        cacheManager.createCache(PRICE_TARGET_RESPONSE_ID_CACHE, getCacheConfiguration(priceTargetResponseCache));
        cacheManager.createCache(PRICE_TARGET_PAGE_CACHE, getCacheConfiguration(pagePriceTargetCache));
        cacheManager.createCache(GOAL_RESPONSE_GOAL_ID_CACHE, getCacheConfiguration(goalResponseCache));
        cacheManager.createCache(PAGE_GOALS_RESPONSE_PAGE_CACHE, getCacheConfiguration(pageGoalsResponseCache));

        return cacheManager;
    }

    private CacheConfiguration<SimpleKey, List<CoingeckoCrypto>> getCoingeckoCryptosCache() {
        Class<List<CoingeckoCrypto>> coinListClass = cast(List.class);

        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(3));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            SimpleKey.class,
            coinListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, CoingeckoCryptoInfo> getCoingeckoCryptoInfoCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(10));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            CoingeckoCryptoInfo.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<SimpleKey, List<UserCrypto>> getAllUserCryptosCache() {
        Class<List<UserCrypto>> userCryptoListClass = cast(List.class);

        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            SimpleKey.class,
            userCryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, List<UserCrypto>> getUserCryptosPlatformIdCache() {
        Class<List<UserCrypto>> userCryptoListClass = cast(List.class);

        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            userCryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, List<UserCrypto>> getUserCryptosCoingeckoCryptoIdCache() {
        Class<List<UserCrypto>> userCryptoListClass = cast(List.class);

        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            userCryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, UserCrypto> getUserCryptoIdCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            UserCrypto.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<Integer, Page<UserCrypto>> getUserCryptosPageCache() {
        Class<Page<UserCrypto>> userCryptoPageClass = cast(Page.class);
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Integer.class,
            userCryptoPageClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<Collection<String>, List<Platform>> getPlatformsIdsCache() {
        Class<Collection<String>> stringCollectionClass = cast(Collection.class);
        Class<List<Platform>> platformListClass = cast(List.class);
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            stringCollectionClass,
            platformListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, Crypto> getCryptoCoingeckoCryptoIdCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            Crypto.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<Collection<String>, List<Crypto>> getCryptosIdsCache() {
        Class<Collection<String>> stringCollectionClass = cast(Collection.class);
        Class<List<Crypto>> cryptoListClass = cast(List.class);
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(2));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            stringCollectionClass,
            cryptoListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<SimpleKey, List<Platform>> getAllPlatformsCache() {
        Class<List<Platform>> platformsListClass = cast(List.class);
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(10));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            SimpleKey.class,
            platformsListClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, Platform> getPlatformCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofDays(10));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            Platform.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, PriceTarget> getPriceTargetCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            PriceTarget.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, PriceTargetResponse> getPriceTargetResponseCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            PriceTargetResponse.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<Integer, Page<PriceTarget>> getPagePriceTargetCache() {
        Class<Page<PriceTarget>> priceTargetPageClass = cast(Page.class);
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Integer.class,
            priceTargetPageClass,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<String, GoalResponse> getGoalResponseCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            String.class,
            GoalResponse.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private CacheConfiguration<Integer, PageGoalResponse> getPageGoalsResponseCache() {
        var resourcePools = ResourcePoolsBuilder.newResourcePoolsBuilder()
            .offheap(1, MemoryUnit.MB)
            .build();
        var expirationPolicyBuilder = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofMinutes(60));

        return CacheConfigurationBuilder.newCacheConfigurationBuilder(
            Integer.class,
            PageGoalResponse.class,
            resourcePools
        ).withExpiry(expirationPolicyBuilder).build();
    }

    private <K, V> javax.cache.configuration.Configuration<K, V> getCacheConfiguration(CacheConfiguration<K, V> cache) {
        return Eh107Configuration.fromEhcacheCacheConfiguration(cache);
    }
}
