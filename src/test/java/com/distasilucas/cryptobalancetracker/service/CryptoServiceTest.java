package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.Image;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData;
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository;
import com.distasilucas.cryptobalancetracker.repository.GoalRepository;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCryptoInfo;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.COINGECKO_CRYPTO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CryptoServiceTest {

    @Mock
    CoingeckoService coingeckoServiceMock;

    @Mock
    CryptoRepository cryptoRepositoryMock;

    @Mock
    UserCryptoRepository userCryptoRepositoryMock;

    @Mock
    GoalRepository goalRepositoryMock;

    @Mock
    Clock clockMock;

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        cryptoService = new CryptoService(coingeckoServiceMock, cryptoRepositoryMock, userCryptoRepositoryMock,
                goalRepositoryMock, clockMock);
    }

    @Test
    void shouldRetrieveCryptoInfoById() {
        var cryptoEntity = getCryptoEntity();
        var expected = new Crypto(
                "bitcoin",
                "Bitcoin",
                "btc",
                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                new BigDecimal("1"),
                new BigDecimal("19000000"),
                new BigDecimal("21000000"),
                LocalDateTime.of(2023, 1, 1, 0, 0, 0)
        );

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.of(cryptoEntity));

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        assertThat(crypto)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldCallRetrieveCryptoInfoAndSaveCryptoWhenRetrievingCryptoInfoById() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var coingeckoCryptoInfo = getCoingeckoCryptoInfo();
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
                localDateTime
        );

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(cryptoEntity)).thenReturn(cryptoEntity);

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(crypto);

        assertThat(crypto)
                .usingRecursiveComparison()
                .isEqualTo(cryptoEntity);
    }

    @Test
    void shouldCallRetrieveCryptoInfoAndSaveCryptoWithZeroMaxSupplyWhenRetrievingCryptoInfoById() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var image = new Image("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579");
        var currentPrice = new CurrentPrice(new BigDecimal("30000"), new BigDecimal("27000"), new BigDecimal("1"));
        var marketDate = new MarketData(currentPrice, new BigDecimal("19000000"), null);
        var coingeckoCryptoInfo = new CoingeckoCryptoInfo("bitcoin", "btc", "Bitcoin", image, marketDate);
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
                localDateTime
        );

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(cryptoEntity)).thenReturn(cryptoEntity);

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(crypto);

        assertThat(crypto)
                .usingRecursiveComparison()
                .isEqualTo(cryptoEntity);
    }

    @Test
    void shouldRetrieveCoingeckoCryptoInfoByName() {
        var coingeckoCrypto = getCoingeckoCrypto();

        when(coingeckoServiceMock.retrieveAllCryptos()).thenReturn(List.of(coingeckoCrypto));

        var crypto = cryptoService.retrieveCoingeckoCryptoInfoByName("bitcoin");

        assertThat(crypto)
                .usingRecursiveComparison()
                .isEqualTo(new CoingeckoCrypto("bitcoin", "btc", "Bitcoin"));
    }

    @Test
    void shouldThrowCoingeckoCryptoNotFoundExceptionWhenRetrievingCoingeckoCryptoInfoByName() {
        var coingeckoCrypto = getCoingeckoCrypto();

        when(coingeckoServiceMock.retrieveAllCryptos()).thenReturn(List.of(coingeckoCrypto));

        var exception = assertThrows(
                CoingeckoCryptoNotFoundException.class,
                () -> cryptoService.retrieveCoingeckoCryptoInfoByName("dogecoin")
        );

        assertEquals(COINGECKO_CRYPTO_NOT_FOUND.formatted("dogecoin"), exception.getMessage());
    }

    @Test
    void shouldSaveCryptoIfNotExists() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var coingeckoCryptoInfo = getCoingeckoCryptoInfo();
        var crypto = new Crypto(
                "bitcoin",
                "Bitcoin",
                "btc",
                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                new BigDecimal("1"),
                new BigDecimal("19000000"),
                new BigDecimal("21000000"),
                localDateTime
        );

        var captor = ArgumentCaptor.forClass(Crypto.class);
        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        cryptoService.saveCryptoIfNotExists("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(crypto);
    }

    @Test
    void shouldSaveCryptoIfNotExistsWithZeroMaxSupply() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var image = new Image("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579");
        var currentPrice = new CurrentPrice(new BigDecimal("30000"), new BigDecimal("27000"), new BigDecimal("1"));
        var marketDate = new MarketData(currentPrice, new BigDecimal("19000000"), null);
        var coingeckoCryptoInfo = new CoingeckoCryptoInfo("bitcoin", "btc", "Bitcoin", image, marketDate);

        var captor = ArgumentCaptor.forClass(Crypto.class);
        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        cryptoService.saveCryptoIfNotExists("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(captor.getValue())
                .usingRecursiveComparison()
                .isEqualTo(
                        new Crypto(
                                "bitcoin",
                                "Bitcoin",
                                "btc",
                                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                                new BigDecimal("30000"),
                                new BigDecimal("27000"),
                                new BigDecimal("1"),
                                new BigDecimal("19000000"),
                                BigDecimal.ZERO,
                                localDateTime
                        )
                );
    }

    @Test
    void shouldNotSaveCryptoIfItAlreadyExists() {
        var crypto = getCryptoEntity();

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.of(crypto));

        cryptoService.saveCryptoIfNotExists("bitcoin");

        verify(cryptoRepositoryMock, never()).save(any());
    }

    @Test
    void shouldDeleteCryptoIfItIsNotBeingUsed() {
        when(userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(Collections.emptyList());
        doNothing().when(cryptoRepositoryMock).deleteById("bitcoin");

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, times(1)).deleteById("bitcoin");
    }

    @Test
    void shouldNotDeleteCryptoIfItsBeingUsedByUserCryptosTable() {
        var userCrypto = getUserCrypto();

        when(userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, never()).deleteById("bitcoin");
    }

    @Test
    void shouldNotDeleteCryptoIfItsBeingUsedByGoalsTable() {
        var goalEntity = getGoalEntity();

        when(goalRepositoryMock.findByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.of(goalEntity));

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, never()).deleteById("bitcoin");
    }

    @Test
    void shouldFindTopCryptosByLastPriceUpdate() {
        var localDateTime = LocalDateTime.now();
        var cryptosEntity = new Crypto(
                "bitcoin",
                "Bitcoin",
                "btc",
                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                new BigDecimal("1"),
                new BigDecimal("19000000"),
                new BigDecimal("21000000"),
                localDateTime
        );

        when(cryptoRepositoryMock.findOldestNCryptosByLastPriceUpdate(localDateTime, 5)).thenReturn(List.of(cryptosEntity));

        var cryptos = cryptoService.findOldestNCryptosByLastPriceUpdate(localDateTime, 5);

        assertThat(cryptos)
                .usingRecursiveComparison()
                .isEqualTo(List.of(cryptosEntity));
    }

    @Test
    void shouldUpdateCryptos() {
        var cryptosEntities = getCryptoEntity();

        when(cryptoRepositoryMock.saveAll(List.of(cryptosEntities))).thenReturn(List.of(cryptosEntities));

        cryptoService.updateCryptos(List.of(cryptosEntities));

        verify(cryptoRepositoryMock, times(1)).saveAll(List.of(cryptosEntities));
    }

}