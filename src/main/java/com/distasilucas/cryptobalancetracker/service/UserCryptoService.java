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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_CRYPTO_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USER_CRYPTO_ID_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserCryptoService {

    private final UserCryptoRepository userCryptoRepository;
    private final PlatformService platformService;
    private final CryptoService cryptoService;

    public UserCrypto findUserCryptoById(String userCryptoId) {
        return userCryptoRepository.findById(userCryptoId)
                .orElseThrow(() -> new UserCryptoNotFoundException(USER_CRYPTO_ID_NOT_FOUND.formatted(userCryptoId)));
    }

    public UserCryptoResponse retrieveUserCryptoById(String userCryptoId) {
        log.info("Retrieving user crypto with id {}", userCryptoId);

        var userCrypto = findUserCryptoById(userCryptoId);
        var crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId());
        var platform = platformService.retrievePlatformById(userCrypto.platformId());

        return userCrypto.toUserCryptoResponse(crypto.name(), platform.name());
    }

    public PageUserCryptoResponse retrieveUserCryptosByPage(int page) {
        log.info("Retrieving user cryptos for page {}", page);

        var pageRequest = PageRequest.of(page, 10);
        var entityUserCryptosPage = userCryptoRepository.findAll(pageRequest);

        var userCryptosPage = entityUserCryptosPage.getContent()
                .stream()
                .map(userCrypto -> {
                    var platform = platformService.retrievePlatformById(userCrypto.platformId());
                    var crypto = cryptoService.retrieveCryptoInfoById(userCrypto.coingeckoCryptoId());

                    return userCrypto.toUserCryptoResponse(crypto.name(), platform.name());
                })
                .toList();

        return new PageUserCryptoResponse(page, entityUserCryptosPage.getTotalPages(), userCryptosPage);
    }

    public UserCryptoResponse saveUserCrypto(UserCryptoRequest userCryptoRequest) {
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(userCryptoRequest.cryptoName());
        var platform = platformService.retrievePlatformById(userCryptoRequest.platformId());

        userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(
                coingeckoCrypto.id(),
                userCryptoRequest.platformId()
        ).ifPresent(userCrypto ->  {
            String message = DUPLICATED_CRYPTO_PLATFORM.formatted(coingeckoCrypto.name(), platform.name());
            throw new DuplicatedCryptoPlatFormException(message);
        });

        var userCrypto = userCryptoRequest.toEntity(coingeckoCrypto.id());

        userCryptoRepository.save(userCrypto);
        log.info("Saved user crypto {}", userCrypto);
        cryptoService.saveCryptoIfNotExists(coingeckoCrypto.id());

        return userCrypto.toUserCryptoResponse(coingeckoCrypto.name(), platform.name());
    }

    public UserCryptoResponse updateUserCrypto(String userCryptoId, UserCryptoRequest userCryptoRequest) {
        var userCrypto = findUserCryptoById(userCryptoId);
        var requestPlatform = platformService.retrievePlatformById(userCryptoRequest.platformId());
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByName(userCryptoRequest.cryptoName());

        if (didChangePlatform(requestPlatform.id(), userCrypto.platformId())) {
            userCryptoRepository.findByCoingeckoCryptoIdAndPlatformId(
                    coingeckoCrypto.id(),
                    userCryptoRequest.platformId()
            ).ifPresent(uc ->  {
                String message = DUPLICATED_CRYPTO_PLATFORM.formatted(coingeckoCrypto.name(), requestPlatform.name());
                throw new DuplicatedCryptoPlatFormException(message);
            });
        }

        var updatedUserCrypto = userCryptoRequest.toEntity(userCrypto.id(), userCrypto.coingeckoCryptoId());

        userCryptoRepository.save(updatedUserCrypto);
        log.info("Updated user crypto. Before: {} | After: {}", userCrypto, updatedUserCrypto);

        return updatedUserCrypto.toUserCryptoResponse(coingeckoCrypto.name(), requestPlatform.name());
    }

    public void deleteUserCrypto(String userCryptoId) {
        var userCrypto = findUserCryptoById(userCryptoId);
        userCryptoRepository.deleteById(userCryptoId);
        cryptoService.deleteCryptoIfNotUsed(userCrypto.coingeckoCryptoId());

        log.info("Deleted user crypto {}", userCryptoId);
    }

    private boolean didChangePlatform(String newPlatform, String originalPlatform) {
        return !newPlatform.equalsIgnoreCase(originalPlatform);
    }
}
