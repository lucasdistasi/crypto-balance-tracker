package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.controller.swagger.InsightsControllerAPI;
import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.SortBy;
import com.distasilucas.cryptobalancetracker.model.SortParams;
import com.distasilucas.cryptobalancetracker.model.SortType;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.service.InsightsService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.constants.Constants.INSIGHTS_ENDPOINT;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;

@Validated
@RestController
@RequestMapping(INSIGHTS_ENDPOINT)
@RequiredArgsConstructor
@CrossOrigin(origins = "${allowed-origins}")
public class InsightsController implements InsightsControllerAPI {

    private final InsightsService insightsService;

    @Override
    @GetMapping("/balances")
    public ResponseEntity<BalancesResponse> retrieveTotalBalancesInsights() {
        var totalBalances = insightsService.retrieveTotalBalancesInsights();

        return ResponseEntity.ok(totalBalances);
    }

    @Override
    @GetMapping("/dates-balances")
    public ResponseEntity<DatesBalanceResponse> retrieveDatesBalancesResponse(@RequestParam DateRange dateRange) {
        var datesBalances = insightsService.retrieveDatesBalances(dateRange);

        return ResponseEntity.ok(datesBalances);
    }

    @Override
    @GetMapping("/cryptos")
    public ResponseEntity<PageUserCryptosInsightsResponse> retrieveUserCryptosInsights(
        @RequestParam
        @Min(value = 0, message = "Page must be greater than or equal to 0")
        int page,
        @RequestParam(required = false, defaultValue = "PERCENTAGE")
        SortBy sortBy,
        @RequestParam(required = false, defaultValue = "DESC")
        SortType sortType
    ) {
        var sortParams = new SortParams(sortBy, sortType);
        var userCryptosInsights = insightsService.retrieveUserCryptosInsights(page, sortParams);

        return okOrNoContent(userCryptosInsights);
    }

    @Override
    @GetMapping("/cryptos/platforms")
    public ResponseEntity<PageUserCryptosInsightsResponse> retrieveUserCryptosPlatformsInsights(
        @RequestParam
        @Min(value = 0, message = "Page must be greater than or equal to 0")
        int page,
        @RequestParam(required = false, defaultValue = "PERCENTAGE")
        SortBy sortBy,
        @RequestParam(required = false, defaultValue = "DESC")
        SortType sortType
    ) {
        var sortParams = new SortParams(sortBy, sortType);
        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(page, sortParams);

        return okOrNoContent(userCryptosPlatformsInsights);
    }

    @Override
    @GetMapping("/cryptos/balances")
    public ResponseEntity<CryptosBalancesInsightsResponse> retrieveCryptosBalancesInsights() {
        var cryptosBalancesInsights = insightsService.retrieveCryptosBalancesInsights();

        return ResponseEntity.ok(cryptosBalancesInsights);
    }

    @Override
    @GetMapping("/platforms/balances")
    public ResponseEntity<PlatformsBalancesInsightsResponse> retrievePlatformsBalancesInsights() {
        var platformsBalancesInsights = insightsService.retrievePlatformsBalancesInsights();

        return ResponseEntity.ok(platformsBalancesInsights);
    }

    @Override
    @GetMapping("/cryptos/{coingeckoCryptoId}")
    public ResponseEntity<CryptoInsightResponse> retrieveCryptoInsights(@PathVariable String coingeckoCryptoId) {
        var cryptoInsights = insightsService.retrieveCryptoInsights(coingeckoCryptoId);

        return ResponseEntity.ok(cryptoInsights);
    }

    @Override
    @GetMapping("/platforms/{platformId}")
    public ResponseEntity<PlatformInsightsResponse> retrievePlatformInsights(
        @PathVariable
        @UUID(message = PLATFORM_ID_UUID)
        String platformId
    ) {
        var platformsInsights = insightsService.retrievePlatformInsights(platformId);

        return ResponseEntity.ok(platformsInsights);
    }

    private <T> ResponseEntity<T> okOrNoContent(Optional<T> body) {
        return body.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }
}
