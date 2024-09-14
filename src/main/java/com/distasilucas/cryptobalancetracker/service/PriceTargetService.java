package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPriceTargetException;
import com.distasilucas.cryptobalancetracker.exception.PriceTargetNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_ID_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_PAGE_CACHE;
import static com.distasilucas.cryptobalancetracker.model.CacheType.PRICE_TARGETS_CACHES;

@Slf4j
@Service
@RequiredArgsConstructor
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS)
public class PriceTargetService {

    private final PriceTargetRepository priceTargetRepository;
    private final CryptoService cryptoService;
    private final CacheService cacheService;
    private final PriceTargetService self;

    @Cacheable(cacheNames = PRICE_TARGET_ID_CACHE, key = "#priceTargetId")
    public PriceTarget retrievePriceTargetById(String priceTargetId) {
        log.info("Retrieving price target for id {}", priceTargetId);

        return priceTargetRepository.findById(priceTargetId)
            .orElseThrow(() -> new PriceTargetNotFoundException(String.format("Price target with id %s not found", priceTargetId)));
    }

    @Cacheable(cacheNames = PRICE_TARGET_PAGE_CACHE, key = "#page")
    public Page<PriceTarget> retrievePriceTargetsByPage(int page) {
        log.info("Retrieving price targets for page {}", page);
        var pageRequest = PageRequest.of(page, 10);

        return priceTargetRepository.findAll(pageRequest);
    }

    public PriceTarget savePriceTarget(PriceTargetRequest priceTargetRequest) {
        log.info("Saving price target {}", priceTargetRequest);

        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId());
        validatePriceTargetIsNotDuplicated(coingeckoCrypto.id(), priceTargetRequest.priceTarget());
        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id());
        var priceTargetEntity = priceTargetRequest.toEntity(crypto);

        var priceTarget = priceTargetRepository.save(priceTargetEntity);
        cacheService.invalidate(PRICE_TARGETS_CACHES);

        return priceTarget;
    }

    public PriceTarget updatePriceTarget(String priceTargetId, PriceTargetRequest priceTargetRequest) {
        log.info("Updating price target for id {}. New value: {}", priceTargetId, priceTargetRequest);

        var priceTarget = self.retrievePriceTargetById(priceTargetId);
        priceTarget.setTarget(priceTargetRequest.priceTarget());

        var coingeckoCryptoId = priceTarget.getCrypto().getId();
        validatePriceTargetIsNotDuplicated(coingeckoCryptoId, priceTargetRequest.priceTarget());

        var updatedPriceTarget = priceTargetRepository.save(priceTarget);
        cacheService.invalidate(PRICE_TARGETS_CACHES);

        return updatedPriceTarget;
    }

    public void deletePriceTarget(String priceTargetId) {
        log.info("Deleting price target for id {}", priceTargetId);
        var priceTarget = self.retrievePriceTargetById(priceTargetId);

        priceTargetRepository.delete(priceTarget);
        cryptoService.deleteCryptoIfNotUsed(priceTarget.getCrypto().getId());
        cacheService.invalidate(PRICE_TARGETS_CACHES);
    }

    private void validatePriceTargetIsNotDuplicated(String coingeckoCryptoId, BigDecimal target) {
        var message = String.format("You already have a price target for %s at that price", coingeckoCryptoId);
        var optionalPriceTarget = priceTargetRepository.findByCoingeckoCryptoIdAndTarget(coingeckoCryptoId, target);

        if (optionalPriceTarget.isPresent()) {
            throw new DuplicatedPriceTargetException(message);
        }
    }
}
