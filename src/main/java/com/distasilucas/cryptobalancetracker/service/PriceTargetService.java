package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPriceTargetException;
import com.distasilucas.cryptobalancetracker.exception.PriceTargetNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PriceTargetService {

    private final PriceTargetRepository priceTargetRepository;
    private final CryptoService cryptoService;

    public PriceTargetResponse retrievePriceTarget(String priceTargetId) {
        log.info("Retrieving price target for id {}", priceTargetId);

        var priceTarget = findById(priceTargetId);
        var crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId());
        var changeNeeded = priceTarget.calculateChangeNeeded(crypto.lastKnownPrice());

        return priceTarget.toPriceTargetResponse(crypto.name(), crypto.lastKnownPrice(), changeNeeded);
    }

    public PagePriceTargetResponse retrievePriceTargetsByPage(int page) {
        log.info("Retrieving price targets for page {}", page);

        var pageRequest = PageRequest.of(page, 10);
        var priceTargets = priceTargetRepository.findAll(pageRequest);
        var priceTargetsResponse = priceTargets.getContent()
            .stream()
            .map(priceTarget -> {
                var crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId());
                return priceTarget.toPriceTargetResponse(crypto.name(), crypto.lastKnownPrice(), priceTarget.calculateChangeNeeded(crypto.lastKnownPrice()));
            })
            .toList();

        return new PagePriceTargetResponse(page, priceTargets.getTotalPages(), priceTargetsResponse);
    }

    public PriceTargetResponse savePriceTarget(PriceTargetRequest priceTargetRequest) {
        log.info("Saving price target {}", priceTargetRequest);

        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId());
        validatePriceTargetIsNotDuplicated(coingeckoCrypto.id(), priceTargetRequest.priceTarget());
        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id());
        var priceTarget = priceTargetRepository.save(priceTargetRequest.toEntity(crypto.id()));

        return priceTarget.toPriceTargetResponse(crypto.name(), crypto.lastKnownPrice(), priceTarget.calculateChangeNeeded(crypto.lastKnownPrice()));
    }

    public PriceTargetResponse updatePriceTarget(String priceTargetId, PriceTargetRequest priceTargetRequest) {
        log.info("Updating price target for id {}. New value: {}", priceTargetId, priceTargetRequest);

        var priceTarget = findById(priceTargetId)
            .withTarget(priceTargetRequest.priceTarget());

        validatePriceTargetIsNotDuplicated(priceTarget.coingeckoCryptoId(), priceTargetRequest.priceTarget());
        var crypto = cryptoService.retrieveCryptoInfoById(priceTarget.coingeckoCryptoId());
        var changeNeeded = priceTarget.calculateChangeNeeded(crypto.lastKnownPrice());
        var newPriceTarget = priceTargetRepository.save(priceTarget);

        return newPriceTarget.toPriceTargetResponse(crypto.name(), crypto.lastKnownPrice(), changeNeeded);
    }

    public void deletePriceTarget(String priceTargetId) {
        log.info("Deleting price target for id {}", priceTargetId);
        var priceTarget = findById(priceTargetId);

        priceTargetRepository.delete(priceTarget);
        cryptoService.deleteCryptoIfNotUsed(priceTarget.coingeckoCryptoId());
    }

    private PriceTarget findById(String priceTargetId) {
        var message = String.format("Price target with id %s not found", priceTargetId);

        return priceTargetRepository.findById(priceTargetId)
            .orElseThrow(() -> new PriceTargetNotFoundException(message));
    }

    private void validatePriceTargetIsNotDuplicated(String coingeckoCryptoId, BigDecimal target) {
        var message = String.format("You already have a price target for %s at that price", coingeckoCryptoId);
        var optionalPriceTarget = priceTargetRepository.findByCoingeckoCryptoIdAndTarget(coingeckoCryptoId, target);

        if (optionalPriceTarget.isPresent()) {
            throw new DuplicatedPriceTargetException(message);
        }
    }

}
