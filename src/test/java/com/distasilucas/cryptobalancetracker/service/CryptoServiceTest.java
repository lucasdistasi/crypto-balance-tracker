package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.view.NonUsedCryptosView;
import com.distasilucas.cryptobalancetracker.exception.CoingeckoCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.Image;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketCap;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData;
import com.distasilucas.cryptobalancetracker.repository.CryptoRepository;
import com.distasilucas.cryptobalancetracker.repository.view.NonUsedCryptosViewRepository;
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
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCryptoInfo;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.COINGECKO_CRYPTO_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class CryptoServiceTest {

    @Mock
    private CoingeckoService coingeckoServiceMock;

    @Mock
    private CryptoRepository cryptoRepositoryMock;

    @Mock
    private NonUsedCryptosViewRepository nonUsedCryptosViewRepositoryMock;

    @Mock
    private CacheService cacheServiceMock;

    @Mock
    private Clock clockMock;

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        cryptoService = new CryptoService(coingeckoServiceMock, cryptoRepositoryMock, nonUsedCryptosViewRepositoryMock,
            cacheServiceMock, clockMock);
    }

    @Test
    void shouldRetrieveCryptoInfoById() {
        var cryptoEntity = getBitcoinCryptoEntity();
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
            1,
            new BigDecimal("813208997089"),
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00"),
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
        var captor = ArgumentCaptor.forClass(Crypto.class);
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
            1,
            new BigDecimal("813208997089"),
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00"),
            localDateTime
        );

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(cryptoEntity);
    }

    @Test
    void shouldCallRetrieveCryptoInfoAndSaveCryptoWithZeroMaxSupplyWhenRetrievingCryptoInfoById() {
        var captor = ArgumentCaptor.forClass(Crypto.class);
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var image = new Image("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579");
        var currentPrice = new CurrentPrice(new BigDecimal("30000"), new BigDecimal("27000"), new BigDecimal("1"));
        var marketDate = new MarketData(
            currentPrice,
            new BigDecimal("19000000"),
            null,
            new MarketCap(new BigDecimal("813208997089")),
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00")
        );
        var coingeckoCryptoInfo = new CoingeckoCryptoInfo("bitcoin", "btc", "Bitcoin", image, 1, marketDate);
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

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(cryptoEntity);
    }

    @Test
    void shouldRetrieveCoingeckoCryptoInfoByName() {
        var coingeckoCrypto = getCoingeckoCrypto();

        when(coingeckoServiceMock.retrieveAllCryptos()).thenReturn(List.of(coingeckoCrypto));

        var crypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin");

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(new CoingeckoCrypto("bitcoin", "btc", "Bitcoin"));
    }

    @Test
    void shouldRetrieveCoingeckoCryptoInfoById() {
        var coingeckoCrypto = new CoingeckoCrypto("wen-4", "wen", "WEN");
        var coingeckoCrypto2 = new CoingeckoCrypto("wen", "wen", "WEN");

        when(coingeckoServiceMock.retrieveAllCryptos()).thenReturn(List.of(coingeckoCrypto, coingeckoCrypto2));

        var crypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId("wen-4");

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(coingeckoCrypto);
    }

    @Test
    void shouldThrowCoingeckoCryptoNotFoundExceptionWhenRetrievingCoingeckoCryptoInfoByName() {
        var coingeckoCrypto = getCoingeckoCrypto();

        when(coingeckoServiceMock.retrieveAllCryptos()).thenReturn(List.of(coingeckoCrypto));

        var exception = assertThrows(
            CoingeckoCryptoNotFoundException.class,
            () -> cryptoService.retrieveCoingeckoCryptoInfoByNameOrId("dogecoin")
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
            1,
            new BigDecimal("813208997089"),
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00"),
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
        verify(cacheServiceMock, times(1)).invalidateCryptosCache();

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
        var marketDate = new MarketData(
            currentPrice,
            new BigDecimal("19000000"),
            null,
            new MarketCap(new BigDecimal("813208997089")),
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00")
        );
        var coingeckoCryptoInfo = new CoingeckoCryptoInfo("bitcoin", "btc", "Bitcoin", image, 1, marketDate);

        var captor = ArgumentCaptor.forClass(Crypto.class);
        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        cryptoService.saveCryptoIfNotExists("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidateCryptosCache();

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
                    1,
                    new BigDecimal("813208997089"),
                    new BigDecimal("10.00"),
                    new BigDecimal("-5.00"),
                    new BigDecimal("0.00"),
                    localDateTime
                )
            );
    }

    @Test
    void shouldNotSaveCryptoIfItAlreadyExists() {
        var crypto = getBitcoinCryptoEntity();

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.of(crypto));

        cryptoService.saveCryptoIfNotExists("bitcoin");

        verify(cryptoRepositoryMock, never()).save(any());
    }

    @Test
    void shouldDeleteCryptoIfItIsNotBeingUsed() {
        when(nonUsedCryptosViewRepositoryMock.findNonUsedCryptosByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.empty());

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, never()).deleteById("bitcoin");
    }

    @Test
    void shouldNotDeleteCryptoIfItsBeingUsedByUserCryptosTable() {
        var nonUsedCryptosView = new NonUsedCryptosView("bitcoin", "Bitcoin", "btc");

        when(nonUsedCryptosViewRepositoryMock.findNonUsedCryptosByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.of(nonUsedCryptosView));

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, times(1)).deleteById("bitcoin");
        verify(cacheServiceMock, times(1)).invalidateCryptosCache();
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
            1,
            new BigDecimal("813208997089"),
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00"),
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
        var cryptosEntities = getBitcoinCryptoEntity();

        when(cryptoRepositoryMock.saveAll(List.of(cryptosEntities))).thenReturn(List.of(cryptosEntities));

        cryptoService.updateCryptos(List.of(cryptosEntities));

        verify(cryptoRepositoryMock, times(1)).saveAll(List.of(cryptosEntities));
    }

    @Test
    void shouldFindAllCryptosById() {
        var crypto = getBitcoinCryptoEntity();

        when(cryptoRepositoryMock.findAllByIdIn(List.of("bitcoin"))).thenReturn(List.of(crypto));

        var cryptos = cryptoService.findAllByIds(List.of("bitcoin"));

        assertThat(cryptos)
            .usingRecursiveComparison()
            .isEqualTo(List.of(crypto));
    }

}
