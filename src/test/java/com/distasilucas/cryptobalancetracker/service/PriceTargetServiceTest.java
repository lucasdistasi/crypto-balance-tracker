package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPriceTargetException;
import com.distasilucas.cryptobalancetracker.exception.PriceTargetNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
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

    private PriceTargetService priceTargetService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        priceTargetService = new PriceTargetService(priceTargetRepositoryMock, cryptoServiceMock);
    }

    @AfterEach
    void tearDown() {
        UUID_MOCK.close();
    }

    @Test
    void shouldRetrievePriceTargetById() {
        var priceTarget = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", new BigDecimal("120000"));

        when(priceTargetRepositoryMock.findById(priceTarget.getId())).thenReturn(Optional.of(priceTarget));
        when(cryptoServiceMock.retrieveCryptoInfoById(priceTarget.getCoingeckoCryptoId())).thenReturn(getBitcoinCryptoEntity());

        var priceTargetResponse = priceTargetService.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

        assertThat(priceTargetResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new PriceTargetResponse(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    "Bitcoin",
                    "30000",
                    "120000",
                    300F
                )
            );
    }

    @Test
    void shouldThrowPriceTargetNotFoundExceptionWhenRetrievingPriceTargetById() {
        when(priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")).thenReturn(Optional.empty());

        var exception = assertThrows(
            PriceTargetNotFoundException.class,
            () -> priceTargetService.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")
        );

        assertEquals("Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found", exception.getMessage());
    }

    @Test
    void shouldRetrievePriceTargetsByPage() {
        var pageRequest = PageRequest.of(0, 10);
        var priceTargets = List.of(
            new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", new BigDecimal("120000")),
            new PriceTarget("ff738ff7-6f9a-400a-8b06-36b7e1fef81e", "bitcoin", new BigDecimal("100000"))
        );
        var pagePriceTargetResponse = new PagePriceTargetResponse(
            0,
            1,
            List.of(
                new PriceTargetResponse(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    "Bitcoin",
                    "30000",
                    "120000",
                    300F
                ),
                new PriceTargetResponse(
                    "ff738ff7-6f9a-400a-8b06-36b7e1fef81e",
                    "Bitcoin",
                    "30000",
                    "100000",
                    233.30F
                )
            )
        );

        when(priceTargetRepositoryMock.findAll(pageRequest)).thenReturn(new PageImpl<>(priceTargets));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(getBitcoinCryptoEntity());

        var priceTargetResponse = priceTargetService.retrievePriceTargetsByPage(0);

        assertThat(priceTargetResponse)
            .usingRecursiveComparison()
            .isEqualTo(pagePriceTargetResponse);
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

        var priceTargetResponse = priceTargetService.savePriceTarget(priceTargetRequest);

        assertThat(priceTargetResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new PriceTargetResponse(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    "Bitcoin",
                    "30000",
                    "120000",
                    300F
                )
            );
        verify(priceTargetRepositoryMock, times(1)).save(captor.getValue());
    }

    @Test
    void shouldThrowDuplicatedPriceTargetExceptionWhenAddingPriceTarget() {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("120000"));
        var coingeckoCrypto = new CoingeckoCrypto("bitcoin", "btc", "Bitcoin");
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", priceTargetRequest.priceTarget());

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId(priceTargetRequest.cryptoNameOrId())).thenReturn(coingeckoCrypto);
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.of(priceTargetEntity));

        var exception = assertThrows(
            DuplicatedPriceTargetException.class,
            () -> priceTargetService.savePriceTarget(priceTargetRequest)
        );

        assertEquals("You already have a price target for bitcoin at that price", exception.getMessage());
    }

    @Test
    void shouldUpdatePriceTarget() {
        var captor = ArgumentCaptor.forClass(PriceTarget.class);
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", new BigDecimal("120000"));

        when(priceTargetRepositoryMock.findById(priceTargetEntity.getId())).thenReturn(Optional.of(priceTargetEntity));
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.empty());
        when(cryptoServiceMock.retrieveCryptoInfoById(priceTargetRequest.cryptoNameOrId()))
            .thenReturn(getBitcoinCryptoEntity());
        when(priceTargetRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var priceTargetResponse = priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest);

        assertThat(priceTargetResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new PriceTargetResponse(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    "Bitcoin",
                    "30000",
                    "100000",
                    233.30F
                )
            );
        verify(priceTargetRepositoryMock, times(1)).save(captor.getValue());
    }

    @Test
    void shouldThrowDuplicatedPriceTargetExceptionWhenUpdatingPriceTarget() {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", priceTargetRequest.priceTarget());
        var anotherSamePriceTargetEntity = new PriceTarget("6fda6f49-9070-4ffa-b9ea-ac52316110d7", "bitcoin", priceTargetRequest.priceTarget());

        when(priceTargetRepositoryMock.findById(priceTargetEntity.getId())).thenReturn(Optional.of(priceTargetEntity));
        when(priceTargetRepositoryMock.findByCoingeckoCryptoIdAndTarget("bitcoin", priceTargetRequest.priceTarget()))
            .thenReturn(Optional.of(anotherSamePriceTargetEntity));

        var exception = assertThrows(
            DuplicatedPriceTargetException.class,
            () -> priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
        );

        assertEquals("You already have a price target for bitcoin at that price", exception.getMessage());
    }

    @Test
    void shouldThrowPriceTargetNotFoundExceptionWhenUpdatingPriceTarget() {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));

        when(priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")).thenReturn(Optional.empty());

        var exception = assertThrows(
            PriceTargetNotFoundException.class,
            () -> priceTargetService.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest)
        );

        assertEquals("Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found", exception.getMessage());
        verify(priceTargetRepositoryMock, never()).save(any());
    }

    @Test
    void shouldDeletePriceTarget() {
        var priceTargetEntity = new PriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "bitcoin", new BigDecimal("120000"));

        when(priceTargetRepositoryMock.findById(priceTargetEntity.getId())).thenReturn(Optional.of(priceTargetEntity));

        priceTargetService.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

        verify(priceTargetRepositoryMock, times(1)).delete(priceTargetEntity);
        verify(cryptoServiceMock, times(1)).deleteCryptoIfNotUsed("bitcoin");
    }

    @Test
    void shouldThrowPriceTargetNotFoundExceptionWhenDeletingPriceTarget() {
        when(priceTargetRepositoryMock.findById("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")).thenReturn(Optional.empty());

        var exception = assertThrows(
            PriceTargetNotFoundException.class,
            () -> priceTargetService.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08")
        );

        assertEquals("Price target with id f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08 not found", exception.getMessage());
        verify(priceTargetRepositoryMock, never()).delete(any());
        verify(cryptoServiceMock, never()).deleteCryptoIfNotUsed(any());
    }

}
