package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPriceTargetException;
import com.distasilucas.cryptobalancetracker.exception.PriceTargetNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.repository.PriceTargetRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.model.CacheType.PRICE_TARGETS_CACHES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PriceTargetServiceTest {

    private final UUID RANDOM_UUID = UUID.fromString("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

    private final MockedStatic<UUID> UUID_MOCK = mockStatic(UUID.class);

    @Mock
    private PriceTargetRepository priceTargetRepositoryMock;

    @Mock
    private CryptoService cryptoServiceMock;

    @Mock
    private CacheService cacheServiceMock;

    @Mock
    private PriceTargetService priceTargetServiceMock;

    private PriceTargetService priceTargetService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        priceTargetService = new PriceTargetService(priceTargetRepositoryMock, cryptoServiceMock, cacheServiceMock, priceTargetServiceMock);
    }

    @AfterEach
    void tearDown() {
        UUID_MOCK.close();
    }

    @Test
    void shouldRetrievePriceTargetById() {
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity());

        when(priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")).thenReturn(Optional.of(priceTargetEntity));

        var priceTarget = priceTargetService.retrievePriceTargetById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

        assertThat(priceTarget)
            .usingRecursiveComparison()
            .isEqualTo(priceTargetEntity);
    }

    @Test
    void shouldThrowPriceTargetNotFoundExceptionWhenRetrievingPriceTargetById() {
        var exceptionMessage = "Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found";

        when(priceTargetServiceMock.retrievePriceTargetById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08"))
            .thenThrow(new PriceTargetNotFoundException(exceptionMessage));

        var exception = assertThrows(
            PriceTargetNotFoundException.class,
            () -> priceTargetService.retrievePriceTargetById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")
        );

        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    void shouldRetrievePriceTargetsByPage() {
        var pageRequest = PageRequest.of(0, 10);
        var priceTargets = List.of(
            new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity()),
            new PriceTarget("ff738ff7-6f9a-400a-8b06-36b7e1fef81e", new BigDecimal("100000"), getBitcoinCryptoEntity())
        );
        var expectedPage = new PageImpl<>(List.of(
            new PriceTarget(
                "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                new BigDecimal("120000"),
                getBitcoinCryptoEntity()
            ),
            new PriceTarget(
                "ff738ff7-6f9a-400a-8b06-36b7e1fef81e",
                new BigDecimal("100000"),
                getBitcoinCryptoEntity()
            )
        ), PageRequest.of(0, 10), 1);

        when(priceTargetRepositoryMock.findAll(pageRequest)).thenReturn(new PageImpl<>(priceTargets, PageRequest.of(0, 10), 1));

        var priceTargetPage = priceTargetService.retrievePriceTargetsByPage(0);

        assertThat(priceTargetPage)
            .usingRecursiveComparison()
            .isEqualTo(expectedPage);
    }

    @Test
    void shouldSavePriceTarget() {
        var captor = ArgumentCaptor.forClass(PriceTarget.class);
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("120000"));
        var coingeckoCrypto = new CoingeckoCrypto("bitcoin", "btc", "Bitcoin");

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId()))
            .thenReturn(coingeckoCrypto);
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.empty());
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(getBitcoinCryptoEntity());
        UUID_MOCK.when(UUID::randomUUID).thenReturn(RANDOM_UUID);
        when(priceTargetRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var priceTarget = priceTargetService.savePriceTarget(priceTargetRequest);

        assertThat(priceTarget)
            .usingRecursiveComparison()
            .isEqualTo(
                new PriceTarget(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    new BigDecimal("120000"),
                    getBitcoinCryptoEntity()
                )
            );
        verify(priceTargetRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidate(PRICE_TARGETS_CACHES);
    }

    @Test
    void shouldThrowDuplicatedPriceTargetExceptionWhenAddingPriceTarget() {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("120000"));
        var coingeckoCrypto = new CoingeckoCrypto("bitcoin", "btc", "Bitcoin");
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest.priceTarget(), getBitcoinCryptoEntity());

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId())).thenReturn(coingeckoCrypto);
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.of(priceTargetEntity));

        var exception = assertThrows(
            DuplicatedPriceTargetException.class,
            () -> priceTargetService.savePriceTarget(priceTargetRequest)
        );

        assertEquals("You already have a price target for bitcoin at that price", exception.getMessage());
        verify(cacheServiceMock, never()).invalidate(any());
    }

    @Test
    void shouldUpdatePriceTarget() {
        var captor = ArgumentCaptor.forClass(PriceTarget.class);
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity());

        when(priceTargetServiceMock.retrievePriceTargetById(priceTargetEntity.getId())).thenReturn(priceTargetEntity);
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.empty());
        when(priceTargetRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var priceTarget = priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest);

        assertThat(priceTarget)
            .usingRecursiveComparison()
            .isEqualTo(
                new PriceTarget(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    new BigDecimal("100000"),
                    getBitcoinCryptoEntity()
                )
            );
        verify(priceTargetRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidate(PRICE_TARGETS_CACHES);
    }

    @Test
    void shouldThrowDuplicatedPriceTargetExceptionWhenUpdatingPriceTarget() {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest.priceTarget(), getBitcoinCryptoEntity());
        var anotherSamePriceTargetEntity = new PriceTarget("6fda6f49-9070-4ffa-b9ea-ac52316110d7", priceTargetRequest.priceTarget(), getBitcoinCryptoEntity());

        when(priceTargetServiceMock.retrievePriceTargetById(priceTargetEntity.getId())).thenReturn(priceTargetEntity);
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.of(anotherSamePriceTargetEntity));

        var exception = assertThrows(
            DuplicatedPriceTargetException.class,
            () -> priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
        );

        assertEquals("You already have a price target for bitcoin at that price", exception.getMessage());
        verify(cacheServiceMock, never()).invalidate(any());
    }

    @Test
    void shouldThrowPriceTargetNotFoundExceptionWhenUpdatingPriceTarget() {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var exceptionMessage = "Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found";

        when(priceTargetServiceMock.retrievePriceTargetById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08"))
            .thenThrow(new PriceTargetNotFoundException(exceptionMessage));

        var exception = assertThrows(
            PriceTargetNotFoundException.class,
            () -> priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
        );

        assertEquals(exceptionMessage, exception.getMessage());
        verify(priceTargetRepositoryMock, never()).save(any());
        verify(cacheServiceMock, never()).invalidate(any());
    }

    @Test
    void shouldDeletePriceTarget() {
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", new BigDecimal("120000"), getBitcoinCryptoEntity());

        when(priceTargetServiceMock.retrievePriceTargetById(priceTargetEntity.getId())).thenReturn(priceTargetEntity);

        priceTargetService.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

        verify(priceTargetRepositoryMock, times(1)).delete(priceTargetEntity);
        verify(cryptoServiceMock, times(1)).deleteCryptoIfNotUsed("bitcoin");
        verify(cacheServiceMock, times(1)).invalidate(PRICE_TARGETS_CACHES);
    }

    @Test
    void shouldThrowPriceTargetNotFoundExceptionWhenDeletingPriceTarget() {
        var exceptionMessage = "Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found";

        when(priceTargetServiceMock.retrievePriceTargetById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08"))
            .thenThrow(new PriceTargetNotFoundException(exceptionMessage));

        var exception = assertThrows(
            PriceTargetNotFoundException.class,
            () -> priceTargetService.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")
        );

        assertEquals(exceptionMessage, exception.getMessage());
        verify(priceTargetRepositoryMock, never()).delete(any());
        verify(cryptoServiceMock, never()).deleteCryptoIfNotUsed(any());
        verify(cacheServiceMock, never()).invalidate(any());
    }

}
