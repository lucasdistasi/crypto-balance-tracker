package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.ChangePercentages;
import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.CryptoInfo;
import com.distasilucas.cryptobalancetracker.entity.LastKnownPrices;
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
import static com.distasilucas.cryptobalancetracker.model.CacheType.CRYPTOS_CACHES;
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
        var expectedCrypto = getCrypto(new BigDecimal("21000000"), LocalDateTime.of(2023, 1, 1, 0, 0, 0));

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.of(cryptoEntity));

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(expectedCrypto);
    }

    @Test
    void shouldCallRetrieveCryptoInfoAndSaveCryptoWhenRetrievingCryptoInfoById() {
        var captor = ArgumentCaptor.forClass(Crypto.class);
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var coingeckoCryptoInfo = getCoingeckoCryptoInfo();
        var expectedCrypto = getCrypto(new BigDecimal("21000000"), localDateTime);

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(expectedCrypto);
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
        var expectedCrypto = getCrypto(BigDecimal.ZERO, localDateTime);

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var crypto = cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(crypto)
            .usingRecursiveComparison()
            .isEqualTo(expectedCrypto);
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
    void shouldSaveCryptoIfNotExistsAndReturn() {
        var localDateTime = LocalDateTime.of(2023, 5, 3, 18, 55, 0);
        var zonedDateTime = ZonedDateTime.of(2023, 5, 3, 19, 0, 0, 0, ZoneId.of("UTC"));
        var coingeckoCryptoInfo = getCoingeckoCryptoInfo();
        var expectedCrypto = getCrypto(new BigDecimal("21000000"), localDateTime);

        var captor = ArgumentCaptor.forClass(Crypto.class);
        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidate(CRYPTOS_CACHES);

        assertThat(captor.getValue())
            .usingRecursiveComparison()
            .isEqualTo(expectedCrypto);
    }

    @Test
    void shouldSaveCryptoIfNotExistsAndReturnWithZeroMaxSupply() {
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
        var expectedCrypto = getCrypto(BigDecimal.ZERO, localDateTime);

        var captor = ArgumentCaptor.forClass(Crypto.class);
        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.empty());
        when(coingeckoServiceMock.retrieveCryptoInfo("bitcoin")).thenReturn(coingeckoCryptoInfo);
        when(clockMock.instant()).thenReturn(localDateTime.toInstant(ZoneOffset.UTC));
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(cryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidate(CRYPTOS_CACHES);

        assertThat(captor.getValue())
            .usingRecursiveComparison()
            .isEqualTo(expectedCrypto);
    }

    @Test
    void shouldNotSaveCryptoIfItAlreadyExists() {
        var crypto = getBitcoinCryptoEntity();

        when(cryptoRepositoryMock.findById("bitcoin")).thenReturn(Optional.of(crypto));

        cryptoService.retrieveCryptoInfoById("bitcoin");

        verify(cryptoRepositoryMock, never()).save(any());
    }

    @Test
    void shouldNotDeleteCryptoIfItsBeingUsedByUserCryptosTable() {
        when(nonUsedCryptosViewRepositoryMock.findNonUsedCryptosByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.empty());

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, never()).deleteById("bitcoin");
        verify(cacheServiceMock, never()).invalidate(any());
    }

    @Test
    void shouldDeleteCryptoIfItIsNotBeingUsed() {
        var nonUsedCryptosView = new NonUsedCryptosView("bitcoin", "Bitcoin", "btc");

        when(nonUsedCryptosViewRepositoryMock.findNonUsedCryptosByCoingeckoCryptoId("bitcoin"))
            .thenReturn(Optional.of(nonUsedCryptosView));

        cryptoService.deleteCryptoIfNotUsed("bitcoin");

        verify(cryptoRepositoryMock, times(1)).deleteById("bitcoin");
        verify(cacheServiceMock, times(1)).invalidate(CRYPTOS_CACHES);
    }

    @Test
    void shouldFindTopCryptosByLastPriceUpdate() {
        var localDateTime = LocalDateTime.now();
        var cryptosEntity = getCrypto(new BigDecimal("21000000"), localDateTime);

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

    private Crypto getCrypto(BigDecimal maxSupply, LocalDateTime lastUpdatedAt) {
        var cryptoInfo = new CryptoInfo(
            "Bitcoin",
            "btc",
            "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
            1,
            new BigDecimal("813208997089"),
            new BigDecimal("19000000"),
            maxSupply
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("30000"),
            new BigDecimal("27000"),
            new BigDecimal("1")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("0.00")
        );

        return new Crypto("bitcoin", cryptoInfo, lastKnownPrices, changePercentages, lastUpdatedAt);
    }

}
