package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPlatformException;
import com.distasilucas.cryptobalancetracker.exception.PlatformNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.ALL_PLATFORMS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_PLATFORMS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_PLATFORM_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.PLATFORM_ID_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PlatformService {

    private final PlatformRepository platformRepository;
    private final UserCryptoRepository userCryptoRepository;
    private final CacheService cacheService;
    private final PlatformService self;

    @Cacheable(cacheNames = ALL_PLATFORMS_CACHE)
    public List<Platform> retrieveAllPlatforms() {
        log.info("Retrieving all platforms");

        return platformRepository.findAll();
    }

    @Cacheable(cacheNames = PLATFORM_PLATFORM_ID_CACHE, key = "#platformId")
    public Platform retrievePlatformById(String platformId) {
        log.info("Retrieving platform with id {}", platformId);

        return platformRepository.findById(platformId)
            .orElseThrow(() -> {
                var message = PLATFORM_ID_NOT_FOUND.formatted(platformId);

                return new PlatformNotFoundException(message);
            });
    }

    public Platform savePlatform(PlatformRequest platformRequest) {
        validatePlatformDoesNotExist(platformRequest.name());
        var platformEntity = platformRequest.toEntity();
        platformRepository.save(platformEntity);
        cacheService.invalidatePlatformsCaches();
        log.info("Saved platform {}", platformEntity);

        return platformEntity;
    }

    public Platform updatePlatform(String platformId, PlatformRequest platformRequest) {
        validatePlatformDoesNotExist(platformRequest.name());
        var platform = self.retrievePlatformById(platformId);
        var updatedPlatform = new Platform(platform.getId(), platformRequest.name().toUpperCase());

        log.info("Updating platform. Before: {}. After: {}", platform, updatedPlatform);
        platformRepository.save(updatedPlatform);
        cacheService.invalidatePlatformsCaches();

        return updatedPlatform;
    }

    public void deletePlatform(String platformId) {
        var platform = self.retrievePlatformById(platformId);
        var userCryptosToDelete = userCryptoRepository.findAllByPlatformId(platformId);
        userCryptoRepository.deleteAll(userCryptosToDelete);
        platformRepository.delete(platform);
        cacheService.invalidatePlatformsCaches();
        cacheService.invalidateUserCryptosCaches();
        log.info("Deleted platform {} and cryptos {}", platform, userCryptosToDelete);
    }

    @Cacheable(cacheNames = PLATFORMS_PLATFORMS_IDS_CACHE, key = "#ids")
    public List<Platform> findAllByIds(Collection<String> ids) {
        log.info("Retrieving platforms for ids {}", ids);

        return platformRepository.findAllByIdIn(ids);
    }

    private void validatePlatformDoesNotExist(String platformName) {
        var platform = platformRepository.findByName(platformName.toUpperCase());

        if (platform.isPresent()) {
            var message = DUPLICATED_PLATFORM.formatted(platformName.toUpperCase());

            throw new DuplicatedPlatformException(message);
        }
    }
}
