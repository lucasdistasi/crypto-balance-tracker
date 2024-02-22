package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.DateBalancesInsightsRange;
import com.distasilucas.cryptobalancetracker.model.SortBy;
import com.distasilucas.cryptobalancetracker.model.SortParams;
import com.distasilucas.cryptobalancetracker.model.SortType;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalances;
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

import java.time.LocalDateTime;
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

        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(Optional.of(balances));

        var totalBalancesInsights = insightsController.retrieveTotalBalancesInsights();

        assertThat(totalBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(balances));
    }

    @Test
    void shouldRetrieveZeroForTotalBalancesWhenEmptyCryptosWithStatus200() {
        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(Optional.empty());

        var totalBalancesInsights = insightsController.retrieveTotalBalancesInsights();

        assertThat(totalBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(new BalancesResponse("0", "0", "0")));
    }

    @Test
    void shouldRetrieveDatesBalancesWithStatus200() {
        var from = LocalDateTime.of(2024, 2, 21, 23, 59, 59);
        var to = LocalDateTime.of(2024, 2, 28, 23, 59, 59);
        var dateBalancesInsightsRange = new DateBalancesInsightsRange(from, to);
        var datesBalances = new DatesBalances("22 February 2024", "1000");
        var datesBalancesResponse = new DatesBalanceResponse(List.of(datesBalances), 5);

        when(insightsServiceMock.retrieveDatesBalances(dateBalancesInsightsRange))
            .thenReturn(Optional.of(datesBalancesResponse));

        var optionalDatesBalances = insightsController.retrieveDatesBalancesResponse(from, to);

        assertThat(optionalDatesBalances)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(datesBalancesResponse));
    }

    @Test
    void shouldRetrieveDatesBalancesWithStatus204() {
        var from = LocalDateTime.of(2024, 2, 21, 23, 59, 59);
        var to = LocalDateTime.of(2024, 2, 28, 23, 59, 59);
        var dateBalancesInsightsRange = new DateBalancesInsightsRange(from, to);

        when(insightsServiceMock.retrieveDatesBalances(dateBalancesInsightsRange))
            .thenReturn(Optional.empty());

        var datesBalances = insightsController.retrieveDatesBalancesResponse(from, to);

        assertThat(datesBalances)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
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

        when(insightsServiceMock.retrieveCryptosBalancesInsights()).thenReturn(Optional.of(cryptosBalancesInsightsResponse));

        var cryptosBalancesInsights = insightsController.retrieveCryptosBalancesInsights();

        assertThat(cryptosBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(cryptosBalancesInsightsResponse));
    }

    @Test
    void shouldRetrieveEmptyForCryptosBalancesInsightsWithStatus204() {
        when(insightsServiceMock.retrieveCryptosBalancesInsights()).thenReturn(Optional.empty());

        var cryptosBalancesInsights = insightsController.retrieveCryptosBalancesInsights();

        assertThat(cryptosBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldRetrievePlatformsBalancesInsightsWithStatus200() {
        var platformsBalancesInsightsResponse = new PlatformsBalancesInsightsResponse(getBalances(), emptyList());

        when(insightsServiceMock.retrievePlatformsBalancesInsights()).thenReturn(Optional.of(platformsBalancesInsightsResponse));

        var platformsBalancesInsights = insightsController.retrievePlatformsBalancesInsights();

        assertThat(platformsBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformsBalancesInsightsResponse));
    }

    @Test
    void shouldRetrieveEmptyForPlatformsBalancesInsightsWithStatus204() {
        when(insightsServiceMock.retrievePlatformsBalancesInsights()).thenReturn(Optional.empty());

        var platformsBalancesInsights = insightsController.retrievePlatformsBalancesInsights();

        assertThat(platformsBalancesInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldRetrieveCryptoInsightsWithStatus200() {
        var cryptoInsightResponse = new CryptoInsightResponse("Bitcoin", getBalances(), emptyList());

        when(insightsServiceMock.retrieveCryptoInsights("bitcoin")).thenReturn(Optional.of(cryptoInsightResponse));

        var cryptoInsights = insightsController.retrieveCryptoInsights("bitcoin");

        assertThat(cryptoInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(cryptoInsightResponse));
    }

    @Test
    void shouldRetrieveEmptyForCryptoInsightsWithStatus204() {
        when(insightsServiceMock.retrieveCryptoInsights("bitcoin")).thenReturn(Optional.empty());

        var cryptoInsights = insightsController.retrieveCryptoInsights("bitcoin");

        assertThat(cryptoInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldRetrievePlatformInsightsWithStatus200() {
        var platformInsightsResponse = new PlatformInsightsResponse("BINANCE", getBalances(), emptyList());

        when(insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111"))
            .thenReturn(Optional.of(platformInsightsResponse));

        var platformInsights = insightsController.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        assertThat(platformInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformInsightsResponse));
    }

    @Test
    void shouldRetrieveEmptyForPlatformInsightsWithStatus204() {
        when(insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")).thenReturn(Optional.empty());

        var platformInsights = insightsController.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        assertThat(platformInsights)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

}
