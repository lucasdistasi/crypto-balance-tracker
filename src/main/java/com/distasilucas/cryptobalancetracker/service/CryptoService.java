package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.ChangePercentages;
import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.CryptoInfo;
import com.distasilucas.cryptobalancetracker.entity.LastKnownPrices;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.entity.view.NonUsedCryptosView;
import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository;
import com.distasilucas.cryptobalancetracker.repository.view.NonUsedCryptosViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.COINGECKO_CRYPTO_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.model.CacheType.CRYPTOS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.PRICE_TARGETS_CACHES;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CoingeckoService coingeckoService;
    private final CryptoRepository cryptoRepository;
    private final NonUsedCryptosViewRepository nonUsedCryptosViewRepository;
    private final CacheService cacheService;
    private final Clock clock;

    @Cacheable(cacheNames = CRYPTO_COINGECKO_CRYPTO_ID_CACHE, key = "#coingeckoCryptoId")
    public Crypto retrieveCryptoInfoById(String coingeckoCryptoId) {
        log.info("Retrieving crypto info for id {}", coingeckoCryptoId);

        return cryptoRepository.findById(coingeckoCryptoId)
            .orElseGet(() -> {
                var crypto = getCrypto(coingeckoCryptoId);
                cryptoRepository.save(crypto);
                cacheService.invalidate(CRYPTOS_CACHES);

                log.info("Saved crypto {}", crypto);

                return crypto;
            });
    }

    public CoingeckoCrypto retrieveCoingeckoCryptoInfoByNameOrId(String cryptoNameOrId) {
        log.info("Retrieving info for coingecko crypto {}", cryptoNameOrId);

        return coingeckoService.retrieveAllCryptos()
            .stream()
            .filter(coingeckoCrypto -> coingeckoCrypto.name().equalsIgnoreCase(cryptoNameOrId) ||
                coingeckoCrypto.id().equalsIgnoreCase(cryptoNameOrId))
            .findFirst()
            .orElseThrow(() -> new CoingeckoCryptoNotFoundException(COINGECKO_CRYPTO_NOT_FOUND.formatted(cryptoNameOrId)));
    }

    public void deleteCryptoIfNotUsed(String coingeckoCryptoId) {
        var nonUsedCryptos = nonUsedCryptosViewRepository.findNonUsedCryptosByCoingeckoCryptoId(coingeckoCryptoId);

        nonUsedCryptos.ifPresent(nonUsedCrypto -> {
            cryptoRepository.deleteById(nonUsedCrypto.getId());
            cacheService.invalidate(CRYPTOS_CACHES);
            log.info("Deleted crypto [{}] - ({}) {} because it was not used", nonUsedCrypto.getId(), nonUsedCrypto.getTicker(), nonUsedCrypto.getName());
        });
    }

    public void deleteCryptosIfNotUsed(List<String> coingeckoCryptoIds) {
        var nonUsedCryptos = nonUsedCryptosViewRepository.findNonUsedCryptosByCoingeckoCryptoIds(coingeckoCryptoIds);

        if (!nonUsedCryptos.isEmpty()) {
            var nonUsedCryptosIds = nonUsedCryptos.stream().map(NonUsedCryptosView::getId).toList();
            cryptoRepository.deleteAllById(nonUsedCryptosIds);
            cacheService.invalidate(CRYPTOS_CACHES);

            log.info("Deleted cryptos {} because they were not used", nonUsedCryptosIds);
        }
    }

    public List<Crypto> findOldestNCryptosByLastPriceUpdate(LocalDateTime localDateTime, int limit) {
        log.info("Retrieving {} cryptos with date filter {}", limit, localDateTime);

        return cryptoRepository.findOldestNCryptosByLastPriceUpdate(localDateTime, limit);
    }

    public void updateCryptos(List<Crypto> cryptosToUpdate) {
        cryptoRepository.saveAll(cryptosToUpdate);
        var cryptosNames = cryptosToUpdate.stream()
            .map(crypto -> crypto.getCryptoInfo().getName())
            .toList();

        log.info("Updated cryptos: {}", cryptosNames);
    }

    @Cacheable(cacheNames = CRYPTOS_CRYPTOS_IDS_CACHE, key = "#ids")
    public List<Crypto> findAllByIds(Collection<String> ids) {
        log.info("Retrieving cryptos with ids {}", ids);

        return cryptoRepository.findAllByIdIn(ids);
    }

    private Crypto getCrypto(String coingeckoCryptoId) {
        var coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(coingeckoCryptoId);
        var marketData = coingeckoCryptoInfo.marketData();
        var cryptoInfo = new CryptoInfo(coingeckoCryptoInfo);
        var lastKnownPrices = new LastKnownPrices(marketData);
        var changePercentages = new ChangePercentages(marketData);

        return new Crypto(coingeckoCryptoId, cryptoInfo, lastKnownPrices, changePercentages, LocalDateTime.now(clock));
    }
}
