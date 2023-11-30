package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedCryptoPlatFormException;
import com.distasilucas.cryptobalancetracker.exception.UserCryptoNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
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

import static com.distasilucas.cryptobalancetracker.TestDataSource.getCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getPlatformEntity;
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
    UserCryptoRepository userCryptoRepositoryMock;

    @Mock
    PlatformService platformServiceMock;

    @Mock
    CryptoService cryptoServiceMock;

    private UserCryptoService userCryptoService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        userCryptoService = new UserCryptoService(userCryptoRepositoryMock, platformServiceMock, cryptoServiceMock);
    }

    @Test
    void shouldFindUserCryptoById() {
        var userCrypto = getUserCrypto();
        var expected = new UserCrypto(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "bitcoin",
                new BigDecimal("0.25"),
                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
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
    void shouldRetrieveUserCrypto() {
        var userCrypto = getUserCrypto();
        var crypto = getCryptoEntity();
        var platform = getPlatformEntity();
        var expected = new UserCryptoResponse(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "Bitcoin",
                "0.25",
                "BINANCE"
        );

        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.of(userCrypto));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(crypto);
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platform);

        var userCryptoResponse = userCryptoService.retrieveUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        assertThat(userCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldThrowUserCryptoNotFoundExceptionIfUserCryptoDoesNotExists() {
        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.empty());

        var exception = assertThrows(
                UserCryptoNotFoundException.class,
                () -> userCryptoService.retrieveUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d")
        );

        assertEquals(USER_CRYPTO_ID_NOT_FOUND.formatted("af827ac7-d642-4461-a73c-b31ca6f6d13d"), exception.getMessage());
    }

    @Test
    void shouldRetrieveUserCryptosByPage() {
        var userCrypto = getUserCrypto();
        var platformEntity = getPlatformEntity();
        var crypto = getCryptoEntity();
        var expected = new PageUserCryptoResponse(
                1,
                1,
                false,
                List.of(userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE"))
        );

        when(userCryptoRepositoryMock.findAll(PageRequest.of(0, 10)))
                .thenReturn(new PageImpl<>(List.of(userCrypto)));
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(crypto);

        var pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0);

        assertThat(pageUserCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldRetrieveUserCryptosByPageWithNextPage() {
        var userCrypto = getUserCrypto();
        var platformEntity = getPlatformEntity();
        var crypto = getCryptoEntity();
        var userCryptosPage = List.of(userCrypto, userCrypto);
        var pageImpl = new PageImpl<>(userCryptosPage, PageRequest.of(0, 2), 10L);
        var expected = new PageUserCryptoResponse(
                1,
                5,
                true,
                List.of(
                        userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE"),
                        userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE")
                )
        );

        when(userCryptoRepositoryMock.findAll(PageRequest.of(0, 10))).thenReturn(pageImpl);
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(crypto);

        var pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0);

        assertThat(pageUserCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldRetrieveEmptyUserCryptosForPage() {
        var expected = new PageUserCryptoResponse(1, 1, false, Collections.emptyList());

        when(userCryptoRepositoryMock.findAll(PageRequest.of(0, 10))).thenReturn(Page.empty());

        var pageUserCryptoResponse = userCryptoService.retrieveUserCryptosByPage(0);

        assertThat(pageUserCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldSaveUserCrypto() {
        var userCryptoRequest = getUserCryptoRequest();
        var coingeckoCrypto = getCoingeckoCrypto();
        var platformEntity = getPlatformEntity();

        var captor = ArgumentCaptor.forClass(UserCrypto.class);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        )).thenReturn(Optional.empty());
        when(userCryptoRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());
        doNothing().when(cryptoServiceMock).saveCryptoIfNotExists("bitcoin");

        var userCryptoResponse = userCryptoService.saveUserCrypto(userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(captor.getValue());

        assertThat(userCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(new UserCryptoResponse(
                        captor.getValue().id(),
                        "Bitcoin",
                        "1",
                        "BINANCE"
                ));
    }

    @Test
    void shouldThrowDuplicatedCryptoPlatFormExceptionWHenSavingUserCrypto() {
        var userCryptoRequest = getUserCryptoRequest();
        var coingeckoCrypto = getCoingeckoCrypto();
        var platformEntity = getPlatformEntity();
        var userCrypto = getUserCrypto();

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
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
        var userCryptoRequest = new UserCryptoRequest("bitcoin", new BigDecimal("1.25"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");
        var userCrypto = getUserCrypto();
        var updatedUserCrypto = new UserCrypto(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "bitcoin",
                new BigDecimal("1.25"),
                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        );
        var platformEntity = getPlatformEntity();
        var coingeckoCrypto = getCoingeckoCrypto();
        var expected = new UserCryptoResponse("af827ac7-d642-4461-a73c-b31ca6f6d13d", "Bitcoin", "1.25", "BINANCE");

        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.of(userCrypto));
        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        )).thenReturn(Optional.empty());
        when(userCryptoRepositoryMock.save(updatedUserCrypto)).thenReturn(updatedUserCrypto);

        var userCryptoResponse =
                userCryptoService.updateUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(updatedUserCrypto);

        assertThat(userCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldFullUpdateUserCrypto() {
        var userCryptoRequest = new UserCryptoRequest("bitcoin", new BigDecimal("1.25"), "123e4567-e89b-12d3-a456-426614174333");
        var userCrypto = getUserCrypto();
        var updatedUserCrypto = new UserCrypto(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "bitcoin",
                new BigDecimal("1.25"),
                "123e4567-e89b-12d3-a456-426614174333"
        );
        var platformEntity = new Platform("123e4567-e89b-12d3-a456-426614174333", "COINBASE");
        var coingeckoCrypto = getCoingeckoCrypto();
        var expected = new UserCryptoResponse("af827ac7-d642-4461-a73c-b31ca6f6d13d", "Bitcoin", "1.25", "COINBASE");

        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.of(userCrypto));
        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
        when(userCryptoRepositoryMock.findByCoingeckoCryptoIdAndPlatformId(
                "bitcoin",
                "123e4567-e89b-12d3-a456-426614174333"
        )).thenReturn(Optional.empty());
        when(userCryptoRepositoryMock.save(updatedUserCrypto)).thenReturn(updatedUserCrypto);

        var userCryptoResponse =
                userCryptoService.updateUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d", userCryptoRequest);

        verify(userCryptoRepositoryMock, times(1)).save(updatedUserCrypto);

        assertThat(userCryptoResponse)
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }

    @Test
    void shouldThrowDuplicatedCryptoPlatFormExceptionWhenUpdatingUserCrypto() {
        var userCryptoRequest = new UserCryptoRequest("bitcoin", new BigDecimal("1.25"), "123e4567-e89b-12d3-a456-426614174333");
        var userCrypto = getUserCrypto();
        var duplicatedUserCrypto = getUserCrypto();
        var platformEntity = new Platform("123e4567-e89b-12d3-a456-426614174333", "COINBASE");
        var coingeckoCrypto = getCoingeckoCrypto();

        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.of(userCrypto));
        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174333")).thenReturn(platformEntity);
        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
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

        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.of(userCrypto));
        doNothing().when(userCryptoRepositoryMock).deleteById("af827ac7-d642-4461-a73c-b31ca6f6d13d");
        doNothing().when(cryptoServiceMock).deleteCryptoIfNotUsed("bitcoin");

        userCryptoService.deleteUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        verify(userCryptoRepositoryMock, times(1)).deleteById("af827ac7-d642-4461-a73c-b31ca6f6d13d");
        verify(cryptoServiceMock, times(1)).deleteCryptoIfNotUsed("bitcoin");
    }

    @Test
    void shouldThrowUserCryptoNotFoundExceptionWHenDeletingUserCrypto() {
        when(userCryptoRepositoryMock.findById("af827ac7-d642-4461-a73c-b31ca6f6d13d")).thenReturn(Optional.empty());

        var exception = assertThrows(
                UserCryptoNotFoundException.class,
                () -> userCryptoService.deleteUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d")
        );

        verify(userCryptoRepositoryMock, never()).deleteById("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        assertEquals(USER_CRYPTO_ID_NOT_FOUND.formatted("af827ac7-d642-4461-a73c-b31ca6f6d13d"), exception.getMessage());
    }

    @Test
    void shouldFindAllByCoingeckoCryptoId() {
        var userCrypto = getUserCrypto();

        when(userCryptoRepositoryMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId("bitcoin");

        assertThat(userCryptos)
                .usingRecursiveComparison()
                .isEqualTo(List.of(
                        new UserCrypto(
                                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                                "bitcoin",
                                new BigDecimal("0.25"),
                                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
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
                                "bitcoin",
                                new BigDecimal("0.25"),
                                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
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
                                "bitcoin",
                                new BigDecimal("0.25"),
                                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
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