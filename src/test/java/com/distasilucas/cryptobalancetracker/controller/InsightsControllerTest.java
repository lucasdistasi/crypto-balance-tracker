package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.SortBy;
import com.distasilucas.cryptobalancetracker.model.SortParams;
import com.distasilucas.cryptobalancetracker.model.SortType;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances;
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.service.InsightsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBalances;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class InsightsControllerTest {

    @Mock
    private InsightsService insightsServiceMock;

    private InsightsController insightsController;

    private static final SortParams sortParams = new SortParams(SortBy.PERCENTAGE, SortType.DESC);

    @BeforeEach
    void setUp() {
        openMocks(this);
        insightsController = new InsightsController(insightsServiceMock);
    }

    @Test
    void shouldRetrieveTotalBalancesWithStatus200() {
        var balances = getBalances();

        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(balances);

        var totalBalancesInsights = insightsController.retrieveTotalBalancesInsights();

        assertThat(totalBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(balances));
    }

    @Test
    void shouldRetrieveZeroForTotalBalancesWhenEmptyCryptosWithStatus200() {
        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(BalancesResponse.empty());

        var totalBalancesInsights = insightsController.retrieveTotalBalancesInsights();

        assertThat(totalBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(new BalancesResponse("0", "0", "0")));
    }

    @Test
    void shouldRetrieveDatesBalancesWithStatus200() {
        var datesBalanceResponse = new DatesBalanceResponse(
            List.of(
                new DateBalances("22 February 2024", new BalancesResponse("1000", "918.45", "0.01438911")),
                new DateBalances("23 February 2024", new BalancesResponse("1500", "1377.67", "0.021583665"))
            ),
            new BalanceChanges(50F, 50F, 49.99F),
            new DifferencesChanges("500", "459.22", "0.007194555")
        );

        when(insightsServiceMock.retrieveDatesBalances(DateRange.ONE_WEEK))
            .thenReturn(datesBalanceResponse);

        var optionalDatesBalances = insightsController.retrieveDatesBalancesResponse(DateRange.ONE_WEEK);

        assertThat(optionalDatesBalances)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(datesBalanceResponse));
    }

    @Test
    void shouldRetrieveCryptosInsightsWithStatus200() {
        var pageUserCryptosInsightsResponse = new PageUserCryptosInsightsResponse(1, 1, getBalances(), emptyList());

        when(insightsServiceMock.retrieveUserCryptosInsights(0, sortParams)).thenReturn(Optional.of(pageUserCryptosInsightsResponse));

        var userCryptosInsights = insightsController.retrieveUserCryptosInsights(0, sortParams.sortBy(), sortParams.sortType());

        assertThat(userCryptosInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(pageUserCryptosInsightsResponse));
    }

    @Test
    void shouldRetrieveEmptyForCryptosInsightsWithStatus204() {
        when(insightsServiceMock.retrieveUserCryptosInsights(0, sortParams)).thenReturn(Optional.empty());

        var userCryptosInsights = insightsController.retrieveUserCryptosInsights(0, sortParams.sortBy(), sortParams.sortType());

        assertThat(userCryptosInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldRetrieveCryptosPlatformsInsightsWithStatus200() {
        var pageUserCryptosInsightsResponse = new PageUserCryptosInsightsResponse(0, 1, getBalances(), emptyList());

        when(insightsServiceMock.retrieveUserCryptosPlatformsInsights(0, sortParams)).thenReturn(Optional.of(pageUserCryptosInsightsResponse));

        var cryptosPlatformsInsights = insightsController.retrieveUserCryptosPlatformsInsights(0, sortParams.sortBy(), sortParams.sortType());

        assertThat(cryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(pageUserCryptosInsightsResponse));
    }

    @Test
    void shouldRetrieveEmptyForCryptosPlatformsInsightsWithStatus204() {
        when(insightsServiceMock.retrieveUserCryptosPlatformsInsights(0, sortParams)).thenReturn(Optional.empty());

        var cryptosPlatformsInsights = insightsController.retrieveUserCryptosPlatformsInsights(0, sortParams.sortBy(), sortParams.sortType());

        assertThat(cryptosPlatformsInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldRetrieveCryptosBalancesInsightsWithStatus200() {
        var cryptosBalancesInsightsResponse = new CryptosBalancesInsightsResponse(getBalances(), emptyList());

        when(insightsServiceMock.retrieveCryptosBalancesInsights()).thenReturn(cryptosBalancesInsightsResponse);

        var cryptosBalancesInsights = insightsController.retrieveCryptosBalancesInsights();

        assertThat(cryptosBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(cryptosBalancesInsightsResponse));
    }

    @Test
    void shouldRetrievePlatformsBalancesInsightsWithStatus200() {
        var platformsBalancesInsightsResponse = new PlatformsBalancesInsightsResponse(getBalances(), emptyList());

        when(insightsServiceMock.retrievePlatformsBalancesInsights()).thenReturn(platformsBalancesInsightsResponse);

        var platformsBalancesInsights = insightsController.retrievePlatformsBalancesInsights();

        assertThat(platformsBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformsBalancesInsightsResponse));
    }

    @Test
    void shouldRetrieveCryptoInsightsWithStatus200() {
        var cryptoInsightResponse = new CryptoInsightResponse("Bitcoin", getBalances(), emptyList());

        when(insightsServiceMock.retrieveCryptoInsights("bitcoin")).thenReturn(cryptoInsightResponse);

        var cryptoInsights = insightsController.retrieveCryptoInsights("bitcoin");

        assertThat(cryptoInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(cryptoInsightResponse));
    }

    @Test
    void shouldRetrievePlatformInsightsWithStatus200() {
        var platformInsightsResponse = new PlatformInsightsResponse("BINANCE", getBalances(), emptyList());

        when(insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111"))
            .thenReturn(platformInsightsResponse);

        var platformInsights = insightsController.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        assertThat(platformInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformInsightsResponse));
    }

}
