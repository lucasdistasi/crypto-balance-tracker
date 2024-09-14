package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedCryptoPlatFormException;
import com.distasilucas.cryptobalancetracker.exception.UserCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_CRYPTO_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USER_CRYPTO_ID_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.model.CacheType.GOALS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.INSIGHTS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.USER_CRYPTOS_CACHES;

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserCryptoService {

    private final UserCryptoRepository userCryptoRepository;
    private final PlatformService platformService;
    private final CryptoService cryptoService;
    private final CacheService cacheService;
    private final UserCryptoService self;

    @Cacheable(cacheNames = USER_CRYPTO_ID_CACHE, key = "#userCryptoId")
    public UserCrypto findUserCryptoById(String userCryptoId) {
        log.info("Retrieving user crypto with id {}", userCryptoId);

        return userCryptoRepository.findById(userCryptoId)
            .orElseThrow(() -> new UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.formatted(userCryptoId)));
    }

    @Cacheable(cacheNames = USER_CRYPTOS_PAGE_CACHE, key = "#page")
    public Page<UserCrypto> retrieveUserCryptosByPage(int page) {
        log.info("Retrieving user cryptos for page {}", page);
        var pageRequest = PageRequest.of(page, 10);

        return userCryptoRepository.findAll(pageRequest);
    }

    public UserCrypto saveUserCrypto(UserCryptoRequest userCryptoRequest) {
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(userCryptoRequest.cryptoName());
        var platform = platformService.retrievePlatformById(userCryptoRequest.platformId());

        userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(
            coingeckoCrypto.id(),
            userCryptoRequest.platformId()
        ).ifPresent(userCrypto -> {
            String message = DUPLICATED_CRYPTO_PLATFORM.formatted(coingeckoCrypto.name(), platform.getName());
            throw new DuplicatedCryptoPlatFormException(message);
        });

        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id());
        var userCrypto = new UserCrypto(userCryptoRequest.quantity(), platform, crypto);
        userCryptoRepository.save(userCrypto);

        log.info("Saved user crypto {}", userCrypto.toSavedUserCryptoString());
        cacheService.invalidate(USER_CRYPTOS_CACHES, GOALS_CACHES, INSIGHTS_CACHES);

        return userCrypto;
    }

    public UserCrypto updateUserCrypto(String userCryptoId, UserCryptoRequest userCryptoRequest) {
        var userCrypto = self.findUserCryptoById(userCryptoId);
        var platform = userCrypto.getPlatform();
        var requestPlatform = platformService.retrievePlatformById(userCryptoRequest.platformId());
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(userCrypto.getCrypto().getId());

        if (didChangePlatform(requestPlatform.getId(), platform.getId())) {
            userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(coingeckoCrypto.id(), userCryptoRequest.platformId())
                .ifPresent(uc -> {
                    String message = DUPLICATED_CRYPTO_PLATFORM.formatted(coingeckoCrypto.name(), requestPlatform.getName());
                    throw new DuplicatedCryptoPlatFormException(message);
                });

            platform = platformService.retrievePlatformById(userCryptoRequest.platformId());
        }

        var updatedUserCrypto = userCrypto.toUpdatedUserCrypto(userCryptoRequest.quantity(), platform);
        log.info("Updating user crypto. Before: {} | After: {}", userCrypto.toUpdatedUserCryptoString(), updatedUserCrypto.toUpdatedUserCryptoString());
        userCryptoRepository.save(updatedUserCrypto);
        cacheService.invalidate(USER_CRYPTOS_CACHES, GOALS_CACHES, INSIGHTS_CACHES);

        return updatedUserCrypto;
    }

    public void deleteUserCrypto(String userCryptoId) {
        var userCrypto = self.findUserCryptoById(userCryptoId);
        userCryptoRepository.deleteById(userCryptoId);
        cryptoService.deleteCryptoIfNotUsed(userCrypto.getCrypto().getId());
        cacheService.invalidate(USER_CRYPTOS_CACHES, GOALS_CACHES, INSIGHTS_CACHES);

        log.info("Deleted user crypto {} from platform {}", userCrypto.getCrypto().getCryptoInfo().getName(), userCrypto.getPlatform().getName());
    }

    public void deleteUserCryptos(List<UserCrypto> userCryptos) {
        if (!userCryptos.isEmpty()) {
            var coingeckoCryptoIds = userCryptos.stream().map(userCrypto -> userCrypto.getCrypto().getId()).toList();
            userCryptoRepository.deleteAll(userCryptos);
            cryptoService.deleteCryptosIfNotUsed(coingeckoCryptoIds);
            cacheService.invalidate(USER_CRYPTOS_CACHES, GOALS_CACHES, INSIGHTS_CACHES);

            log.info("Deleted user cryptos {}", coingeckoCryptoIds);
        }
    }

    @Cacheable(cacheNames = USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE, key = "#coingeckoCryptoId")
    public List<UserCrypto> findAllByCoingeckoCryptoId(String coingeckoCryptoId) {
        log.info("Retrieving all user cryptos matching coingecko crypto id {}", coingeckoCryptoId);

        return userCryptoRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId);
    }

    public Optional<UserCrypto> findByCoingeckoCryptoIdAndPlatformId(String cryptoId, String platformId) {
        return userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(cryptoId, platformId);
    }

    public void saveOrUpdateAll(List<UserCrypto> userCryptos) {
        userCryptoRepository.saveAll(userCryptos);
        cacheService.invalidate(USER_CRYPTOS_CACHES, GOALS_CACHES, INSIGHTS_CACHES);
    }

    @Cacheable(cacheNames = USER_CRYPTOS_CACHE)
    public List<UserCrypto> findAll() {
        log.info("Retrieving all user cryptos");

        return userCryptoRepository.findAll();
    }

    @Cacheable(cacheNames = USER_CRYPTOS_PLATFORM_ID_CACHE, key = "#platformId")
    public List<UserCrypto> findAllByPlatformId(String platformId) {
        log.info("Retrieving all user cryptos for platformId {}", platformId);

        return userCryptoRepository.findAllByPlatformId(platformId);
    }

    private boolean didChangePlatform(String newPlatform, String originalPlatform) {
        return !newPlatform.equalsIgnoreCase(originalPlatform);
    }
}
