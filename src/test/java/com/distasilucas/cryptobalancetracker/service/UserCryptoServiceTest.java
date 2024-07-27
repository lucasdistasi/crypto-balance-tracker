package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedCryptoPlatFormException;
import com.distasilucas.cryptobalancetracker.exception.UserCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.repository.UserCryptoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getBinancePlatformEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_CRYPTO_PLATFORM;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.USER_CRYPTO_ID_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class UserCryptoServiceTest {

    @Mock
    private UserCryptoRepository userCryptoRepositoryMock;

    @Mock
    private PlatformService platformServiceMock;

    @Mock
    private CryptoService cryptoServiceMock;

    @Mock
    private CacheService cacheServiceMock;

    @Mock
    private UserCryptoService userCryptoServiceMock;

    private UserCryptoService userCryptoService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        userCryptoService = new UserCryptoService(userCryptoRepositoryMock, platformServiceMock, cryptoServiceMock, cacheServiceMock, userCryptoServiceMock);
    }

    @Test
    void shouldFindUserCryptoById() {
        var userCrypto = getUserCrypto();
        var platformEntity = getBinancePlatformEntity();
        var expected = new UserCrypto(
            "af827ac7-d642-4461-a73c-b31ca6f6d13d",
            new BigDecimal("0.25"),
            platformEntity,
            getBitcoinCryptoEntity()
        );

        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.of(userCrypto));

        var response = userCryptoService.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        assertThat(response)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void shouldThrowUserCryptoNotFoundExceptionIfUserCryptoDoesNotExist() {
        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.empty());

        var exception = assertThrows(
            UserCryptoNotFoundException.class,
            () -> userCryptoService.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")
        );

        assertEquals(USER_CRYPTO_ID_NOT_FOUND.formatted("af827ac7-d642-4461-a73c-b31ca6f6d13d"), exception.getMessage());
    }

    @Test
    void shouldRetrieveUserCryptosByPage() {
        var userCrypto = getUserCrypto();
        var expected = new PageImpl<>(Collections.singletonList(userCrypto), PageRequest.of(0, 10), 1);

        when(userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)))
            .thenReturn(new PageImpl<>(List.of(userCrypto)));

        var pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0);

        assertThat(pageUserCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void shouldRetrieveUserCryptosByPageWithNextPage() {
        var userCrypto = getUserCrypto();
        var userCryptos = List.of(userCrypto, userCrypto);
        var pageImpl = new PageImpl<>(userCryptos, PageRequest.of(0, 2), 10L);

        when(userCryptoRepositoryMock.findAll(PageRequest.of(0, 10))).thenReturn(pageImpl);

        var pageUserCrypto = userCryptoService.retrieveUserCryptosByPage(0);

        assertThat(pageUserCrypto)
            .usingRecursiveComparison()
            .isEqualTo(pageImpl);
    }

    @Test
    void shouldRetrieveEmptyUserCryptosForPage() {
        when(userCryptoRepositoryMock.findAll(PageRequest.of(0, 10))).thenReturn(Page.empty());

        var pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0);

        assertThat(pageUserCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(Page.empty());
    }

    @Test
    void shouldSaveUserCrypto() {
        var userCryptoRequest = getUserCryptoRequest();
        var coingeckoCrypto = getCoingeckoCrypto();
        var platformEntity = getBinancePlatformEntity();

        var captor = ArgumentCaptor.forClass(UserCrypto.class);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
            "bitcoin",
            "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        )).thenReturn(Optional.empty());
        when(userCryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(getBitcoinCryptoEntity());

        var userCryptoResponse = userCryptoService.saveUserCrypto(userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidateUserCryptosAndInsightsCaches();
        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(new UserCrypto(
                captor.getValue().getId(),
                new BigDecimal("1"),
                getBinancePlatformEntity(),
                getBitcoinCryptoEntity()
            ));
    }

    @Test
    void shouldThrowDuplicatedCryptoPlatFormExceptionWHenSavingUserCrypto() {
        var userCryptoRequest = getUserCryptoRequest();
        var coingeckoCrypto = getCoingeckoCrypto();
        var platformEntity = getBinancePlatformEntity();
        var userCrypto = getUserCrypto();

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
            "bitcoin",
            "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        )).thenReturn(Optional.of(userCrypto));

        var exception = assertThrows(
            DuplicatedCryptoPlatFormException.class,
            () -> userCryptoService.saveUserCrypto(userCryptoRequest)
        );

        verify(userCryptoRepositoryMock, never()).save(any());

        assertEquals(DUPLICATED_CRYPTO_PLATFORM.formatted("Bitcoin", "BINANCE"), exception.getMessage());
    }

    @Test
    void shouldUpdateQuantityUserCrypto() {
        var captor = ArgumentCaptor.forClass(UserCrypto.class);
        var userCryptoRequest = new UserCryptoRequest("bitcoin", new BigDecimal("1.25"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");
        var userCrypto = getUserCrypto();
        var platformEntity = getBinancePlatformEntity();
        var coingeckoCrypto = getCoingeckoCrypto();
        var expected = new UserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", new BigDecimal("1.25"), getBinancePlatformEntity(), getBitcoinCryptoEntity());

        when(userCryptoServiceMock.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(userCrypto);
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
            "bitcoin",
            "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        )).thenReturn(Optional.empty());
        when(userCryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var userCryptoResponse =
            userCryptoService.updateUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidateUserCryptosAndInsightsCaches();
        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void shouldFullUpdateUserCrypto() {
        var captor = ArgumentCaptor.forClass(UserCrypto.class);
        var userCryptoRequest = new UserCryptoRequest("bitcoin", new BigDecimal("1.25"), "123e4567-e89b-12d3-a456-426614174333");
        var userCrypto = getUserCrypto();
        var platformEntity = new Platform("123e4567-e89b-12d3-a456-426614174333", "COINBASE");
        var coingeckoCrypto = getCoingeckoCrypto();
        var expected = new UserCrypto(
            "af827ac7-d642-4461-a73c-b31ca6f6d13d",
            new BigDecimal("1.25"),
            platformEntity,
            getBitcoinCryptoEntity()
        );

        when(userCryptoServiceMock.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(userCrypto);
        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
            "bitcoin",
            "123e4567-e89b-12d3-a456-426614174333"
        )).thenReturn(Optional.empty());
        when(userCryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var userCryptoResponse =
            userCryptoService.updateUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidateUserCryptosAndInsightsCaches();
        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void shouldNotUpdateCryptoNameNorId() {
        var captor = ArgumentCaptor.forClass(UserCrypto.class);
        var userCryptoRequest = new UserCryptoRequest("ethereum", new BigDecimal("1.25"), "123e4567-e89b-12d3-a456-426614174333");
        var platformEntity = new Platform("123e4567-e89b-12d3-a456-426614174333", "COINBASE");
        var coingeckoCrypto = getCoingeckoCrypto();
        var userCrypto = new UserCrypto(
            "af827ac7-d642-4461-a73c-b31ca6f6d13d",
            new BigDecimal("0.25"),
            platformEntity,
            getBitcoinCryptoEntity()
        );

        when(userCryptoServiceMock.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(userCrypto);
        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(userCryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var userCryptoResponse =
            userCryptoService.updateUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidateUserCryptosAndInsightsCaches();

        assertThat(userCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(new UserCrypto(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                new BigDecimal("1.25"),
                platformEntity,
                getBitcoinCryptoEntity()
            ));
    }

    @Test
    void shouldThrowDuplicatedCryptoPlatFormExceptionWhenUpdatingUserCrypto() {
        var userCryptoRequest = new UserCryptoRequest("bitcoin", new BigDecimal("1.25"), "123e4567-e89b-12d3-a456-426614174333");
        var userCrypto = getUserCrypto();
        var duplicatedUserCrypto = getUserCrypto();
        var platformEntity = new Platform("123e4567-e89b-12d3-a456-426614174333", "COINBASE");
        var coingeckoCrypto = getCoingeckoCrypto();

        when(userCryptoServiceMock.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(userCrypto);
        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
            "bitcoin",
            "123e4567-e89b-12d3-a456-426614174333"
        )).thenReturn(Optional.of(duplicatedUserCrypto));

        var exception = assertThrows(
            DuplicatedCryptoPlatFormException.class,
            () -> userCryptoService.updateUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", userCryptoRequest)
        );

        verify(userCryptoRepositoryMock, never()).save(any());

        assertEquals(DUPLICATED_CRYPTO_PLATFORM.formatted("Bitcoin", "COINBASE"), exception.getMessage());
    }

    @Test
    void shouldDeleteUserCrypto() {
        var userCrypto = getUserCrypto();

        when(userCryptoServiceMock.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(userCrypto);
        doNothing().when(userCryptoRepositoryMock).deleteById("af827ac7-d642-4461-a73c-b31ca6f6d13d");
        doNothing().when(cryptoServiceMock).deleteCryptoIfNotUsed("bitcoin");

        userCryptoService.deleteUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        verify(userCryptoRepositoryMock, times(1)).deleteById("af827ac7-d642-4461-a73c-b31ca6f6d13d");
        verify(cryptoServiceMock, times(1)).deleteCryptoIfNotUsed("bitcoin");
        verify(cacheServiceMock, times(1)).invalidateUserCryptosAndInsightsCaches();
    }

    @Test
    void shouldThrowUserCryptoNotFoundExceptionWHenDeletingUserCrypto() {
        var exceptionMessage = USER_CRYPTO_ID_NOT_FOUND.formatted("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        when(userCryptoServiceMock.findUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d"))
            .thenThrow(new UserCryptoNotFoundException(exceptionMessage));

        var exception = assertThrows(
            UserCryptoNotFoundException.class,
            () -> userCryptoService.deleteUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d")
        );

        verify(userCryptoRepositoryMock, never()).deleteById("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        assertEquals(exceptionMessage, exception.getMessage());
    }

    @Test
    void shouldFindAllByCoingeckoCryptoId() {
        var userCrypto = getUserCrypto();
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId("bitcoin");

        assertThat(userCryptos)
            .usingRecursiveComparison()
            .isEqualTo(List.of(
                new UserCrypto(
                    "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                    new BigDecimal("0.25"),
                    platformEntity,
                    getBitcoinCryptoEntity()
                )
            ));
    }

    @Test
    void shouldFindByCoingeckoCryptoIdAndPlatformId() {
        var userCrypto = getUserCrypto();

        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "123e4567-e89b-12d3-a456-426614174111"))
            .thenReturn(Optional.of(userCrypto));

        var response =
            userCryptoService.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "123e4567-e89b-12d3-a456-426614174111");

        assertTrue(response.isPresent());
        assertThat(response.get())
            .usingRecursiveComparison()
            .isEqualTo(userCrypto);
    }


    @Test
    void shouldSaveOrUpdateAll() {
        var userCryptos = List.of(getUserCrypto());

        when(userCryptoRepositoryMock.saveAll(userCryptos)).thenReturn(userCryptos);

        userCryptoService.saveOrUpdateAll(userCryptos);

        verify(userCryptoRepositoryMock, times(1)).saveAll(userCryptos);
        verify(cacheServiceMock, times(1)).invalidateUserCryptosAndInsightsCaches();
    }

    @Test
    void shouldFindAllUserCryptos() {
        var userCrypto = getUserCrypto();

        when(userCryptoRepositoryMock.findAll()).thenReturn(List.of(userCrypto));

        var userCryptos = userCryptoService.findAll();

        assertThat(userCryptos)
            .usingRecursiveComparison()
            .isEqualTo(List.of(
                new UserCrypto(
                    "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                    new BigDecimal("0.25"),
                    getBinancePlatformEntity(),
                    getBitcoinCryptoEntity()
                )
            ));
    }

    @Test
    void shouldFindAllUserCryptosByPlatformId() {
        var userCrypto = getUserCrypto();

        when(userCryptoRepositoryMock.findAllByPlatformId("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(List.of(userCrypto));

        var userCryptos = userCryptoService.findAllByPlatformId("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        assertThat(userCryptos)
            .usingRecursiveComparison()
            .isEqualTo(List.of(
                new UserCrypto(
                    "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                    new BigDecimal("0.25"),
                    getBinancePlatformEntity(),
                    getBitcoinCryptoEntity()
                )
            ));
    }

    private UserCryptoRequest getUserCryptoRequest() {
        return new UserCryptoRequest("bitcoin", new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");
    }

    private CoingeckoCrypto getCoingeckoCrypto() {
        return new CoingeckoCrypto("bitcoin", "btc", "Bitcoin");
    }

}
