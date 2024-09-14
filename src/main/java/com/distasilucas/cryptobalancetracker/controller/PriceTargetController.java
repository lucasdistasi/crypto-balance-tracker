package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.controller.swagger.PriceTargetControllerAPI;
import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import com.distasilucas.cryptobalancetracker.service.PriceTargetService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.distasilucas.cryptobalancetracker.constants.Constants.PRICE_TARGET_ENDPOINT;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PRICE_TARGET_UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(PRICE_TARGET_ENDPOINT)
@CrossOrigin(origins = "${allowed-origins}")
public class PriceTargetController implements PriceTargetControllerAPI {

    private final PriceTargetService priceTargetService;

    @Override
    @GetMapping("/{priceTargetId}")
    public ResponseEntity<PriceTargetResponse> retrievePriceTarget(
        @PathVariable @UUID(message = INVALID_PRICE_TARGET_UUID) String priceTargetId
    ) {
        var priceTarget = priceTargetService.retrievePriceTargetById(priceTargetId);

        return ResponseEntity.ok(priceTarget.toPriceTargetResponse());
    }

    @Override
    @GetMapping
    public ResponseEntity<PagePriceTargetResponse> retrievePriceTargetsByPage(
        @RequestParam @Min(value = 0, message = INVALID_PAGE_NUMBER) int page
    ) {
        var priceTargets = priceTargetService.retrievePriceTargetsByPage(page);

        if (priceTargets.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            var priceTargetResponses = priceTargets.getContent()
                .stream()
                .map(PriceTarget::toPriceTargetResponse)
                .toList();
            var pagePriceTargetResponse = new PagePriceTargetResponse(page, priceTargets.getTotalPages(), priceTargetResponses);

            return ResponseEntity.ok(pagePriceTargetResponse);
        }
    }

    @Override
    @PostMapping
    public ResponseEntity<PriceTargetResponse> savePriceTarget(
        @Valid @RequestBody PriceTargetRequest priceTargetRequest
    ) {
        var priceTarget = priceTargetService.savePriceTarget(priceTargetRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(priceTarget.toPriceTargetResponse());
    }

    @Override
    @PutMapping("/{priceTargetId}")
    public ResponseEntity<PriceTargetResponse> updatePriceTarget(
        @PathVariable @UUID(message = INVALID_PRICE_TARGET_UUID) String priceTargetId,
        @Valid @RequestBody PriceTargetRequest priceTargetRequest
    ) {
        var priceTarget = priceTargetService.updatePriceTarget(priceTargetId, priceTargetRequest);

        return ResponseEntity.ok(priceTarget.toPriceTargetResponse());
    }

    @Override
    @DeleteMapping("/{priceTargetId}")
    public ResponseEntity<Void> deletePriceTarget(
        @PathVariable @UUID(message = INVALID_PRICE_TARGET_UUID) String priceTargetId
    ) {
        priceTargetService.deletePriceTarget(priceTargetId);

        return ResponseEntity.noContent().build();
    }
}
