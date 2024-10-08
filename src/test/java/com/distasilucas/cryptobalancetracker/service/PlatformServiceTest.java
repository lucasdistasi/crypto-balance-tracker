package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPlatformException;
import com.distasilucas.cryptobalancetracker.exception.PlatformNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBinancePlatformEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.PLATFORM_ID_NOT_FOUND;
import static com.distasilucas.cryptobalancetracker.model.CacheType.INSIGHTS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.PLATFORMS_CACHES;
import static com.distasilucas.cryptobalancetracker.model.CacheType.USER_CRYPTOS_CACHES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PlatformServiceTest {

    @Mock
    private PlatformRepository platformRepositoryMock;

    @Mock
    private UserCryptoService userCryptoServiceMock;

    @Mock
    private CacheService cacheServiceMock;

    @Mock
    private PlatformService platformServiceMock;

    private PlatformService platformService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        platformService = new PlatformService(platformRepositoryMock, userCryptoServiceMock, cacheServiceMock, platformServiceMock);
    }

    @Test
    void shouldRetrieveAllPlatforms() {
        var platform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var platforms = List.of(platform);

        when(platformRepositoryMock.findAll()).thenReturn(platforms);

        var allPlatforms = platformService.retrieveAllPlatforms();

        assertThat(allPlatforms)
            .usingRecursiveComparison()
            .isEqualTo(platforms);
    }

    @Test
    void shouldReturnEmptyIfNoPlatformsAreSaved() {
        when(platformRepositoryMock.findAll()).thenReturn(Collections.emptyList());

        var allPlatforms = platformService.retrieveAllPlatforms();

        assertThat(allPlatforms)
            .usingRecursiveComparison()
            .isEqualTo(Collections.emptyList());
    }

    @Test
    void shouldRetrievePlatformById() {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(platformRepositoryMock.findById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
            .thenReturn(Optional.of(platformEntity));

        var platform = platformService.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        assertThat(platform)
            .usingRecursiveComparison()
            .isEqualTo(platformEntity);
    }

    @Test
    void shouldThrowPlatformNotFoundExceptionWhenPlatformWasNotFound() {
        var expectedMessage = PLATFORM_ID_NOT_FOUND.formatted("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        when(platformRepositoryMock.findById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(Optional.empty());
        var exception = assertThrows(
            PlatformNotFoundException.class,
            () -> platformService.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldSavePlatformSuccessfully() {
        var platformRequest = new PlatformRequest("binance");
        var platformArgumentCaptor = ArgumentCaptor.forClass(Platform.class);

        when(platformRepositoryMock.save(platformArgumentCaptor.capture())).thenAnswer(answer -> platformArgumentCaptor.getValue());

        var platform = platformService.savePlatform(platformRequest);

        verify(platformRepositoryMock, times(1)).save(platformArgumentCaptor.getValue());
        verify(cacheServiceMock, times(1)).invalidate(PLATFORMS_CACHES);
        assertThat(platform)
            .usingRecursiveComparison()
            .isEqualTo(new Platform(platformArgumentCaptor.getValue().getId(), "BINANCE"));
    }

    @Test
    void shouldThrowDuplicatedPlatformExceptionWhenSavingPlatform() {
        var platformRequest = new PlatformRequest("binance");
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var expectedMessage = DUPLICATED_PLATFORM.formatted("BINANCE");

        when(platformRepositoryMock.findByName("BINANCE")).thenReturn(Optional.of(platformEntity));
        var exception = assertThrows(DuplicatedPlatformException.class, () -> platformService.savePlatform(platformRequest));

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldUpdatePlatformSuccessfully() {
        var platformArgumentCaptor = ArgumentCaptor.forClass(Platform.class);
        var platformRequest = new PlatformRequest("bybit");
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BYBIT");
        var existingPlatform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(existingPlatform);
        when(platformRepositoryMock.save(platformArgumentCaptor.capture())).thenAnswer(answer -> platformArgumentCaptor.getValue());

        var platform = platformService.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest);

        verify(platformRepositoryMock, times(1)).save(platformArgumentCaptor.getValue());
        verify(cacheServiceMock, times(1)).invalidate(PLATFORMS_CACHES, USER_CRYPTOS_CACHES, INSIGHTS_CACHES);
        assertThat(platform)
            .usingRecursiveComparison()
            .isEqualTo(platformEntity);
    }

    @Test
    void shouldThrowDuplicatedPlatformExceptionWhenUpdatingPlatform() {
        var platformRequest = new PlatformRequest("binance");
        var existingPlatform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var expectedMessage = DUPLICATED_PLATFORM.formatted("BINANCE");

        when(platformRepositoryMock.findByName("BINANCE")).thenReturn(Optional.of(existingPlatform));
        var exception = assertThrows(
            DuplicatedPlatformException.class,
            () -> platformService.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldThrowPlatformNotFoundExceptionWhenUpdatingPlatform() {
        var platformRequest = new PlatformRequest("bybit");
        var expectedMessage = PLATFORM_ID_NOT_FOUND.formatted("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
            .thenThrow(new PlatformNotFoundException(expectedMessage));

        var exception = assertThrows(
            PlatformNotFoundException.class,
            () -> platformService.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldDeletePlatformSuccessfully() {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var userCrypto = new UserCrypto(
            "af827ac7-d642-4461-a73c-b31ca6f6d13d",
            new BigDecimal("1"),
            getBinancePlatformEntity(),
            getBitcoinCryptoEntity()
        );

        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(userCryptoServiceMock.findAllByPlatformId("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
            .thenReturn(List.of(userCrypto));

        platformService.deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        verify(userCryptoServiceMock, times(1)).deleteUserCryptos(List.of(userCrypto));
        verify(platformRepositoryMock, times(1)).delete(platformEntity);
        verify(cacheServiceMock, times(1)).invalidate(PLATFORMS_CACHES, INSIGHTS_CACHES);
    }

    @Test
    void shouldThrowPlatformNotFoundExceptionWhenDeletingPlatform() {
        var expectedMessage = PLATFORM_ID_NOT_FOUND.formatted("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
            .thenThrow(new PlatformNotFoundException(expectedMessage));

        var exception = assertThrows(
            PlatformNotFoundException.class,
            () -> platformService.deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldRetrieveAllPlatformsById() {
        var platformsIds = List.of("e86b1068-8635-4606-83fb-a056040d6c9e", "d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8");
        var platforms = List.of(
            new Platform("e86b1068-8635-4606-83fb-a056040d6c9e", "BINANCE"),
            new Platform("d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8", "COINBASE")
        );

        when(platformRepositoryMock.findAllByIdIn(platformsIds)).thenReturn(platforms);

        var platformsList = platformService.findAllByIds(
            List.of("e86b1068-8635-4606-83fb-a056040d6c9e", "d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8")
        );

        assertThat(platformsList)
            .usingRecursiveComparison()
            .isEqualTo(
                List.of(
                    new Platform("e86b1068-8635-4606-83fb-a056040d6c9e", "BINANCE"),
                    new Platform("d6fa4d2a-7760-4e7b-9df3-eabb26a92dd8", "COINBASE")
                )
            );
    }

}
