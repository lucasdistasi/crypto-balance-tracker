package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedCryptoPlatFormException;
import com.distasilucas.cryptobalancetracker.exception.UserCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_CRYPTO_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USER_CRYPTO_ID_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCryptoService {

    private final UserCryptoRepository userCryptoRepository;
    private final PlatformService platformService;
    private final CryptoService cryptoService;
    private final CacheService cacheService;

    public UserCrypto findUserCryptoById(String userCryptoId) {
        log.info("Retrieving user crypto with id {}", userCryptoId);

        return userCryptoRepository.findById(userCryptoId)
            .orElseThrow(() -> new UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.formatted(userCryptoId)));
    }

    public UserCryptoResponse retrieveUserCryptoById(String userCryptoId) {
        log.info("Retrieving user crypto with id {}", userCryptoId);

        var userCrypto = findUserCryptoById(userCryptoId);
        var crypto = cryptoService.retrieveCryptoInfoById(userCrypto.getCrypto().getId());
        var platform = platformService.retrievePlatformById(userCrypto.getPlatform().getId());

        return userCrypto.toUserCryptoResponse(crypto.getName(), platform.getName());
    }

    public PageUserCryptoResponse retrieveUserCryptosByPage(int page) {
        log.info("Retrieving user cryptos for page {}", page);

        var pageRequest = PageRequest.of(page, 10);
        var entityUserCryptosPage = userCryptoRepository.findAll(pageRequest);

        var userCryptosPage = entityUserCryptosPage.getContent()
            .stream()
            .map(userCrypto -> {
                var platform = platformService.retrievePlatformById(userCrypto.getPlatform().getId());
                var crypto = cryptoService.retrieveCryptoInfoById(userCrypto.getCrypto().getId());

                return userCrypto.toUserCryptoResponse(crypto.getName(), platform.getName());
            })
            .toList();

        return new PageUserCryptoResponse(page, entityUserCryptosPage.getTotalPages(), userCryptosPage);
    }

    public UserCryptoResponse saveUserCrypto(UserCryptoRequest userCryptoRequest) {
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
        log.info("Saved user crypto {}", userCrypto);
        cacheService.invalidateUserCryptosCaches();

        return userCrypto.toUserCryptoResponse(coingeckoCrypto.name(), platform.getName());
    }

    public UserCryptoResponse updateUserCrypto(String userCryptoId, UserCryptoRequest userCryptoRequest) {
        var userCrypto = findUserCryptoById(userCryptoId);
        var platform = userCrypto.getPlatform();
        var requestPlatform = platformService.retrievePlatformById(userCryptoRequest.platformId());
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(userCrypto.getCrypto().getId());

        if (didChangePlatform(requestPlatform.getId(), platform.getId())) {
            userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(coingeckoCrypto.id(), userCryptoRequest.platformId())
                .ifPresent(uc -> {
                    String message = DUPLICATED_CRYPTO_PLATFORM.formatted(coingeckoCrypto.name(), requestPlatform.getName());
                    throw new DuplicatedCryptoPlatFormException(message);
                });
        }

        var updatedUserCrypto = userCrypto.toUpdatedUserCrypto(userCryptoRequest.quantity(), platform);

        userCryptoRepository.save(updatedUserCrypto);
        cacheService.invalidateUserCryptosCaches();
        log.info("Updated user crypto. Before: {} | After: {}", userCrypto, updatedUserCrypto);

        return updatedUserCrypto.toUserCryptoResponse(coingeckoCrypto.name(), requestPlatform.getName());
    }

    public void deleteUserCrypto(String userCryptoId) {
        var userCrypto = findUserCryptoById(userCryptoId);
        userCryptoRepository.deleteById(userCryptoId);
        cryptoService.deleteCryptoIfNotUsed(userCrypto.getCrypto().getId());
        cacheService.invalidateUserCryptosCaches();

        log.info("Deleted user crypto {}", userCryptoId);
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
        cacheService.invalidateUserCryptosCaches();
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
