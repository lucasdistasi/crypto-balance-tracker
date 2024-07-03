package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository;
import com.distasilucas.cryptobalancetracker.repository.view.NonUsedCryptosViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_CRYPTOS_IDS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_COINGECKO_CRYPTO_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.COINGECKO_CRYPTO_NOT_FOUND;

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

        var cryptoOptional = cryptoRepository.findById(coingeckoCryptoId);

        if (cryptoOptional.isPresent()) {
            return cryptoOptional.get();
        }

        var cryptoToSave = getCrypto(coingeckoCryptoId);

        log.info("Saved crypto {} because it didn't exist", cryptoToSave);

        return cryptoRepository.save(cryptoToSave);
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

    public Crypto saveCryptoIfNotExistsAndReturn(String coingeckoCryptoId) {
        return cryptoRepository.findById(coingeckoCryptoId)
            .orElseGet(() -> {
                var crypto = getCrypto(coingeckoCryptoId);
                cryptoRepository.save(crypto);
                cacheService.invalidateCryptosCache();

                log.info("Saved crypto {}", crypto);

                return crypto;
            });
    }

    public void deleteCryptoIfNotUsed(String coingeckoCryptoId) {
        var nonUsedCryptos = nonUsedCryptosViewRepository.findNonUsedCryptosByCoingeckoCryptoId(coingeckoCryptoId);

        nonUsedCryptos.ifPresent(nonUsedCrypto -> {
            cryptoRepository.deleteById(nonUsedCrypto.getId());
            cacheService.invalidateCryptosCache();
            log.info("Deleted crypto [{}] - ({}){} because it was not used", nonUsedCrypto.getId(), nonUsedCrypto.getTicker(), nonUsedCrypto.getName());
        });
    }

    public List<Crypto> findOldestNCryptosByLastPriceUpdate(LocalDateTime localDateTime, int limit) {
        log.info("Retrieving {} cryptos with date filter {}", limit, localDateTime);

        return cryptoRepository.findOldestNCryptosByLastPriceUpdate(localDateTime, limit);
    }

    public void updateCryptos(List<Crypto> cryptosToUpdate) {
        cryptoRepository.saveAll(cryptosToUpdate);
        var cryptosNames = cryptosToUpdate.stream()
            .map(Crypto::getName)
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
        var maxSupply = marketData.maxSupply() != null ?
            marketData.maxSupply() :
            BigDecimal.ZERO;

        return new Crypto(
            coingeckoCryptoId,
            coingeckoCryptoInfo.name(),
            coingeckoCryptoInfo.symbol(),
            coingeckoCryptoInfo.image().large(),
            marketData.currentPrice().usd(),
            marketData.currentPrice().eur(),
            marketData.currentPrice().btc(),
            marketData.circulatingSupply(),
            maxSupply,
            coingeckoCryptoInfo.marketCapRank(),
            marketData.marketCap().usd(),
            marketData.changePercentageIn24h(),
            marketData.changePercentageIn7d(),
            marketData.changePercentageIn30d(),
            LocalDateTime.now(clock)
        );
    }
}
