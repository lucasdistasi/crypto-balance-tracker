package com.distasilucas.cryptobalancetracker.scheduler;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException;
import com.distasilucas.cryptobalancetracker.service.CoingeckoService;
import com.distasilucas.cryptobalancetracker.service.CryptoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class CryptoScheduler {

    private final int maxLimit;
    private final Clock clock;
    private final CryptoService cryptoService;
    private final CoingeckoService coingeckoService;

    public CryptoScheduler(
        @Value("${max-limit-crypto}") int maxLimit,
        Clock clock,
        CryptoService cryptoService,
        CoingeckoService coingeckoService
    ) {
        this.maxLimit = maxLimit;
        this.clock = clock;
        this.cryptoService = cryptoService;
        this.coingeckoService = coingeckoService;
    }

    @Scheduled(cron = "${update-crypto-info-cron}")
    public void updateCryptosInformation() {
        log.info("Running cron to update cryptos...");

        var cryptosToUpdate = getCryptosToUpdate()
            .stream()
            .map(this::mapCrypto)
            .toList();

        if (cryptosToUpdate.isEmpty()) {
            log.info("No cryptos to update");
        } else {
            log.info("About to update {} crypto(s)", cryptosToUpdate.size());

            cryptoService.updateCryptos(cryptosToUpdate);
        }
    }

    private List<Crypto> getCryptosToUpdate() {
        return cryptoService.findOldestNCryptosByLastPriceUpdate(LocalDateTime.now(clock).minusMinutes(5), maxLimit);
    }

    private Crypto mapCrypto(Crypto crypto) {
        try {
            var coingeckoCrypto = coingeckoService.retrieveCryptoInfo(crypto.getId());

            return new Crypto(coingeckoCrypto, LocalDateTime.now(clock));
        } catch (RestClientResponseException exception) {
            if (HttpStatus.TOO_MANY_REQUESTS == exception.getStatusCode()) {
                throw new TooManyRequestsException();
            } else {
                log.warn("A RestClientResponseException occurred while retrieving info for {}", crypto.getId(), exception);
                return crypto;
            }
        } catch (Exception exception) {
            log.error("An exception occurred while retrieving info for {}, therefore crypto info might be outdated", crypto.getId(), exception);

            return crypto;
        }
    }

}
