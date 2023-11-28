package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository;
import com.distasilucas.cryptobalancetracker.repository.GoalRepository;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.COINGECKO_CRYPTO_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class CryptoService {

    private final CoingeckoService coingeckoService;
    private final CryptoRepository cryptoRepository;
    private final UserCryptoRepository userCryptoRepository;
    private final GoalRepository goalRepository;
    private final Clock clock;

    public Crypto retrieveCryptoInfoById(String coingeckoCryptoId) {
        log.info("Retrieving crypto info for id {}", coingeckoCryptoId);

        var cryptoOptional = cryptoRepository.findById(coingeckoCryptoId);

        if (cryptoOptional.isPresent()) {
            return cryptoOptional.get();
        }

        var coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(coingeckoCryptoId);
        var maxSupply = coingeckoCryptoInfo.marketData().maxSupply() != null ?
                coingeckoCryptoInfo.marketData().maxSupply() :
                BigDecimal.ZERO;

        var cryptoToSave = new Crypto(
                coingeckoCryptoId,
                coingeckoCryptoInfo.name(),
                coingeckoCryptoInfo.symbol(),
                coingeckoCryptoInfo.image().large(),
                coingeckoCryptoInfo.marketData().currentPrice().usd(),
                coingeckoCryptoInfo.marketData().currentPrice().eur(),
                coingeckoCryptoInfo.marketData().currentPrice().btc(),
                coingeckoCryptoInfo.marketData().circulatingSupply(),
                maxSupply,
                LocalDateTime.now(clock)
        );

        log.info("Saved crypto {} because it didn't exist", cryptoToSave);

        return cryptoRepository.save(cryptoToSave);
    }

    public CoingeckoCrypto retrieveCoingeckoCryptoInfoByName(String cryptoName) {
        log.info("Retrieving info for coingecko crypto {}", cryptoName);

        return coingeckoService.retrieveAllCryptos()
                .stream()
                .filter(coingeckoCrypto -> coingeckoCrypto.name().equalsIgnoreCase(cryptoName))
                .findFirst()
                .orElseThrow(() -> new CoingeckoCryptoNotFoundException(COINGECKO_CRYPTO_NOT_FOUND.formatted(cryptoName)));
    }

    public void saveCryptoIfNotExists(String coingeckoCryptoId) {
        var cryptoOptional = cryptoRepository.findById(coingeckoCryptoId);

        if (cryptoOptional.isEmpty()) {
            var coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo(coingeckoCryptoId);
            var maxSupply = coingeckoCryptoInfo.marketData().maxSupply() != null ?
                    coingeckoCryptoInfo.marketData().maxSupply() :
                    BigDecimal.ZERO;

            var crypto = new Crypto(
                    coingeckoCryptoId,
                    coingeckoCryptoInfo.name(),
                    coingeckoCryptoInfo.symbol(),
                    coingeckoCryptoInfo.image().large(),
                    coingeckoCryptoInfo.marketData().currentPrice().usd(),
                    coingeckoCryptoInfo.marketData().currentPrice().eur(),
                    coingeckoCryptoInfo.marketData().currentPrice().btc(),
                    coingeckoCryptoInfo.marketData().circulatingSupply(),
                    maxSupply,
                    LocalDateTime.now(clock)
            );

            cryptoRepository.save(crypto);
            log.info("Saved crypto {}", crypto);
        }
    }

    public void deleteCryptoIfNotUsed(String coingeckoCryptoId) {
        var userCryptos = userCryptoRepository.findAllByCoingeckoCryptoId(coingeckoCryptoId);

        if (userCryptos.isEmpty()) {
            var goal = goalRepository.findByCoingeckoCryptoId(coingeckoCryptoId);

            if (goal.isEmpty()) {
                cryptoRepository.deleteById(coingeckoCryptoId);
                log.info("Deleted crypto {} because it was not used", coingeckoCryptoId);
            }
        }
    }

    public List<Crypto> findOldestNCryptosByLastPriceUpdate(LocalDateTime localDateTime, int limit) {
        log.info("Retrieving {} cryptos with date filter {}", limit, localDateTime);

        return cryptoRepository.findOldestNCryptosByLastPriceUpdate(localDateTime, limit);
    }

    public void updateCryptos(List<Crypto> cryptosToUpdate) {
        cryptoRepository.saveAll(cryptosToUpdate);
        var cryptosNames = cryptosToUpdate.stream()
                .map(Crypto::name)
                .toList();

        log.info("Updated cryptos: {}", cryptosNames);
    }
}
