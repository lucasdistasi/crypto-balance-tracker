package com.distasilucas.cryptobalancetracker.scheduler;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.exception.TooManyRequestsException;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.Image;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketCap;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData;
import com.distasilucas.cryptobalancetracker.service.CoingeckoService;
import com.distasilucas.cryptobalancetracker.service.CryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCryptoInfo;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.REQUEST_LIMIT_REACHED;
import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CryptoSchedulerTest {

    private static final int LIMIT = 9;
    private static final Long MINUTES = 5L;

    @Mock
    private Clock clockMock;

    @Mock
    private CryptoService cryptoServiceMock;

    @Mock
    private CoingeckoService coingeckoServiceMock;

    private CryptoScheduler cryptoScheduler;

    @BeforeEach
    void setUp() {
        openMocks(this);
        cryptoScheduler = new CryptoScheduler(LIMIT, clockMock, cryptoServiceMock, coingeckoServiceMock);
    }

    @Test
    void shouldUpdateTop9CryptosInformationFromTheLast5Minutes() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var cryptoEntity = new Crypto(
                "bitcoin",
                "Bitcoin",
                "btc",
                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                new BigDecimal("1"),
                new BigDecimal("19000000"),
                new BigDecimal("21000000"),
                1,
                new BigDecimal("813208997089"),
                new BigDecimal("10.00"),
                new BigDecimal("-5.00"),
                new BigDecimal("0.00"),
                localDateTime
        );
        var coingeckoCryptoInfo = getCoingeckoCryptoInfo();

        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(localDateTime.minusMinutes(MINUTES), LIMIT))
                .thenReturn(List.of(cryptoEntity));
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        doNothing().when(cryptoServiceMock).updateCryptos(List.of(cryptoEntity));

        cryptoScheduler.updateCryptosInformation();

        verify(coingeckoServiceMock, times(1)).retrieveCryptoInfo("bitcoin");
        verify(cryptoServiceMock, times(1)).updateCryptos(List.of(cryptoEntity));
    }

    @Test
    void shouldUpdateTop9CryptosInformationFromTheLast5MinutesWithZeroAsMaxSupply() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var cryptoEntity = new Crypto(
                "bitcoin",
                "Bitcoin",
                "btc",
                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                new BigDecimal("1"),
                new BigDecimal("19000000"),
                BigDecimal.ZERO,
                1,
                new BigDecimal("813208997089"),
                new BigDecimal("10.00"),
                new BigDecimal("-5.00"),
                new BigDecimal("0.00"),
                localDateTime
        );
        var image = new Image("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579");
        var currentPrice = new CurrentPrice(new BigDecimal("30000"), new BigDecimal("27000"), new BigDecimal("1"));
        var marketData = new MarketData(
                currentPrice,
                new BigDecimal("19000000"),
                null,
                new MarketCap(new BigDecimal("813208997089")),
                new BigDecimal("10.00"),
                new BigDecimal("-5.00"),
                new BigDecimal("0.00")
        );
        var coingeckoCryptoInfo = new CoingeckoCryptoInfo("bitcoin", "btc", "Bitcoin", image, 1, marketData);

        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(localDateTime.minusMinutes(MINUTES), LIMIT))
                .thenReturn(List.of(cryptoEntity));
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        doNothing().when(cryptoServiceMock).updateCryptos(List.of(cryptoEntity));

        cryptoScheduler.updateCryptosInformation();

        verify(coingeckoServiceMock, times(1)).retrieveCryptoInfo("bitcoin");
        verify(cryptoServiceMock, times(1)).updateCryptos(List.of(cryptoEntity));
    }

    @Test
    void shouldNotUpdateIfThereAreNoCryptosToUpdate() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));

        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(localDateTime.minusMinutes(MINUTES), LIMIT)).thenReturn(emptyList());

        cryptoScheduler.updateCryptosInformation();

        verify(cryptoServiceMock, never()).updateCryptos(any());
    }

    @Test
    void shouldThrowTooManyRequestsExceptionWhenReachingCoingeckoLimit() {
        var crypto = getBitcoinCryptoEntity();
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var queryLocalDateTime = LocalDateTime.of(2023, 5, 3, 18, 50, 0);
        var restClientResponseException = new RestClientResponseException("message", HttpStatus.TOO_MANY_REQUESTS, "statusText", null, null, null);

        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(queryLocalDateTime, LIMIT)).thenReturn(List.of(crypto));
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenThrow(restClientResponseException);

        var exception = assertThrows(
                TooManyRequestsException.class,
                () -> cryptoScheduler.updateCryptosInformation()
        );

        assertEquals(REQUEST_LIMIT_REACHED, exception.getMessage());
        verify(cryptoServiceMock, never()).updateCryptos(any());
    }

    @Test
    void shouldSaveSameCryptoWhenRestClientResponseExceptionOccursWithStatusNot429() {
        var crypto = getBitcoinCryptoEntity();
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var queryLocalDateTime = LocalDateTime.of(2023, 5, 3, 18, 50, 0);
        var restClientResponseException = new RestClientResponseException("message", HttpStatus.I_AM_A_TEAPOT, "statusText", null, null, null);

        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(queryLocalDateTime, LIMIT)).thenReturn(List.of(crypto));
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenThrow(restClientResponseException);
        doNothing().when(cryptoServiceMock).updateCryptos(List.of(crypto));

        cryptoScheduler.updateCryptosInformation();

        verify(cryptoServiceMock, times(1)).updateCryptos(List.of(crypto));
    }

    @Test
    void shouldSaveSameCryptoIfExceptionOccursWhenRetrievingCryptoInfo() {
        var crypto = getBitcoinCryptoEntity();
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var queryLocalDateTime = LocalDateTime.of(2023, 5, 3, 18, 50, 0);

        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoServiceMock.findOldestNCryptosByLastPriceUpdate(queryLocalDateTime, LIMIT)).thenReturn(List.of(crypto));
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenThrow(new RuntimeException("Some exception"));
        doNothing().when(cryptoServiceMock).updateCryptos(List.of(crypto));

        cryptoScheduler.updateCryptosInformation();

        verify(cryptoServiceMock, times(1)).updateCryptos(List.of(crypto));
    }

}