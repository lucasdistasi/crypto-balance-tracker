package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedPlatformException;
import com.distasilucas.cryptobalancetracker.exception.PlatformNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.repository.PlatformRepository;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.PLATFORM_ID_NOT_FOUND;
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
    private UserCryptoRepository userCryptoRepositoryMock;

    @Mock
    private CacheService cacheServiceMock;

    private PlatformService platformService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        platformService = new PlatformService(platformRepositoryMock, userCryptoRepositoryMock, cacheServiceMock);
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
        verify(cacheServiceMock, times(1)).invalidatePlatformsCaches();
        assertThat(platform)
                .usingRecursiveComparison()
                .isEqualTo(new Platform(platformArgumentCaptor.getValue().id(), "BINANCE"));
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
        var platformRequest = new PlatformRequest("bybit");
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BYBIT");
        var existingPlatform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(platformRepositoryMock.findById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(Optional.of(existingPlatform));
        when(platformRepositoryMock.save(platformEntity)).thenReturn(platformEntity);

        var platform = platformService.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest);

        verify(platformRepositoryMock, times(1)).save(platformEntity);
        verify(cacheServiceMock, times(1)).invalidatePlatformsCaches();
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

        when(platformRepositoryMock.findById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(Optional.empty());
        var exception = assertThrows(
                PlatformNotFoundException.class,
                () -> platformService.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest)
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void shouldDeletePlatformSuccessfully() {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(platformRepositoryMock.findById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(Optional.of(platformEntity));

        platformService.deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        verify(platformRepositoryMock, times(1)).delete(platformEntity);
        verify(cacheServiceMock, times(1)).invalidatePlatformsCaches();
        verify(cacheServiceMock, times(1)).invalidateUserCryptosCaches();
    }

    @Test
    void shouldThrowPlatformNotFoundExceptionWhenDeletingPlatform() {
        var expectedMessage = PLATFORM_ID_NOT_FOUND.formatted("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        when(platformRepositoryMock.findById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(Optional.empty());
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