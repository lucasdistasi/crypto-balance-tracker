package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.ApiValidationException;
import com.distasilucas.cryptobalancetracker.exception.InsufficientBalanceException;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.TransferCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.FromPlatform;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.ToPlatform;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.TransferCryptoResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.NOT_ENOUGH_BALANCE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.SAME_FROM_TO_PLATFORM;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class TransferCryptoServiceTest {

    private final UUID RANDOM_UUID = UUID.fromString("60560fe6-8be2-460f-89ba-ef2e1c2e405b");

    private final MockedStatic<UUID> UUID_MOCK = mockStatic(UUID.class);

    private final Platform BYBIT_PLATFORM = new Platform("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9", "BYBIT");
    private final Platform BINANCE_PLATFORM = new Platform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "BINANCE");

    @Mock
    private UserCryptoService userCryptoServiceMock;

    @Mock
    private PlatformService platformServiceMock;

    private TransferCryptoService transferCryptoService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        transferCryptoService = new TransferCryptoService(userCryptoServiceMock, platformServiceMock);
    }

    @AfterEach
    void tearDown() {
        UUID_MOCK.close();
    }

    //      FROM        |       TO             |
    //  has remaining   |   has the crypto     | ---> Update FROM and TO.
    //  has remaining   |   hasn't the crypto  | ---> Update FROM. Add to TO.
    //  no remaining    |   has the crypto     | ---> Remove it from FROM. Update TO.
    //  no remaining    |   hasn't the crypto  | ---> It's easier to update FROM with the new platform and quantity.

    /*
        If there is remaining in from platform, we need to check full quantity toggle to perform some operations based on that

        totalToSubtract (full quantity enabled) = quantityToTransfer + networkFee
        totalToSubtract (full quantity disabled) = quantityToTransfer
        quantityToSendReceive (full quantity enabled) = quantityToTransfer
        quantityToSendReceive (full quantity disabled) = quantityToTransfer - networkFee
        newQuantity = quantity (actual quantity from toPlatformUserCrypto if there is or ZERO) + quantityToSendReceive
     */

    @Test
    void shouldTransferFromPlatformWithRemainingToPlatformWithExistingCryptoAndFullQuantityDisabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = getTransferCryptoRequest(false);
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatformUserCrypto = getToPlatformUserCrypto();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9"))
            .thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479"))
            .thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.of(toPlatformUserCrypto));
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "0.51",
                        "0.51",
                        "0.5095",
                        "1.865321283",
                        false
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "2.261938292")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithRemainingToPlatformWithoutExistingCryptoAndFullQuantityDisabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = getTransferCryptoRequest(false);
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.empty());
        UUID_MOCK.when(UUID::randomUUID).thenReturn(RANDOM_UUID);
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "0.51",
                        "0.51",
                        "0.5095",
                        "1.865321283",
                        false
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "0.5095")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithRemainingToPlatformWithExistingCryptoAndFullQuantityEnabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = getTransferCryptoRequest(true);
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatformUserCrypto = getToPlatformUserCrypto();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.of(toPlatformUserCrypto));
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "0.51",
                        "0.5105",
                        "0.51",
                        "1.864821283",
                        true
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "2.262438292")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithRemainingToPlatformWithoutExistingCryptoAndFullQuantityEnabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = getTransferCryptoRequest(true);
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.empty());
        UUID_MOCK.when(UUID::randomUUID).thenReturn(RANDOM_UUID);
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "0.51",
                        "0.5105",
                        "0.51",
                        "1.864821283",
                        true
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "0.51")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithRemainingToPlatformWithoutExistingCryptoAndFullQuantityDisabledAndUpdateOnlyOneCrypto() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("0.51"),
            new BigDecimal("0.51"),
            false,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("2.375321283"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(fromPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(toPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.empty());
        UUID_MOCK.when(UUID::randomUUID).thenReturn(RANDOM_UUID);
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.51",
                        "0.51",
                        "0.51",
                        "0",
                        "1.865321283",
                        false
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "0")
                )
            );
    }

    /*
        If there is no remaining in from platform, it does not matter if full quantity is true or false.

        totalToSubtract = quantityToTransfer
        quantityToSendReceive = quantityToTransfer - networkFee
        newQuantity = quantity (actual quantity from toPlatformUserCrypto if there is or ZERO) + quantityToSendReceive
     */

    @Test
    void shouldTransferFromPlatformWithoutRemainingToPlatformWithExistingCryptoAndFullQuantityEnabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("1.105734142"),
            new BigDecimal("0.0005"),
            true,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("1.105734142"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatformUserCrypto = new UserCrypto(
            "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
            new BigDecimal("0.2512"),
            BINANCE_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.of(toPlatformUserCrypto));
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "1.105734142",
                        "1.105734142",
                        "1.105234142",
                        "0",
                        true
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "1.356434142")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithoutRemainingToPlatformWithoutExistingCryptoAndFullQuantityEnabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("1.105734142"),
            new BigDecimal("0.0005"),
            true,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("1.105734142"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.empty());
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "1.105734142",
                        "1.105734142",
                        "1.105234142",
                        "0",
                        true
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "1.105234142")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithoutRemainingToPlatformWithExistingCryptoAndFullQuantityDisabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("1.105734142"),
            new BigDecimal("0.0005"),
            false,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("1.105734142"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatformUserCrypto = getToPlatformUserCrypto();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.of(toPlatformUserCrypto));
        doNothing().when(userCryptoServiceMock).deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "1.105734142",
                        "1.105734142",
                        "1.105234142",
                        "0",
                        false
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "2.857672434")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithoutRemainingToPlatformWithoutExistingCryptoAndFullQuantityDisabled() {
        Class<List<UserCrypto>> listClass = (Class<List<UserCrypto>>)(Class)List.class;
        ArgumentCaptor<List<UserCrypto>> captor = ArgumentCaptor.forClass(listClass);
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("2.375321283"),
            new BigDecimal("0.0005"),
            false,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.empty());
        doAnswer(answer -> captor.getValue()).when(userCryptoServiceMock).saveOrUpdateAll(captor.capture());

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, times(1)).saveOrUpdateAll(captor.getValue());
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "0.0005",
                        "2.375321283",
                        "2.375321283",
                        "2.374821283",
                        "0",
                        false
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "2.374821283")
                )
            );
    }

    @Test
    void shouldTransferFromPlatformWithoutRemainingToPlatformWithoutExistingCryptoAndFullQuantityDisabledAndDeleteOneCrypto() {
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("2.375321283"),
            new BigDecimal("2.375321283"),
            false,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);
        when(userCryptoServiceMock.findByCoingeckoCryptoIdAndPlatformId("bitcoin", "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"))
            .thenReturn(Optional.empty());
        doNothing().when(userCryptoServiceMock).deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479");

        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        verify(userCryptoServiceMock, never()).saveOrUpdateAll(anyList());
        verify(userCryptoServiceMock, times(1)).deleteUserCrypto("f47ac10b-58cc-4372-a567-0e02b2c3d479");
        assertThat(transferCryptoResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new TransferCryptoResponse(
                    new FromPlatform(
                        "f47ac10b-58cc-4372-a567-0e02b2c3d479",
                        "2.375321283",
                        "2.375321283",
                        "2.375321283",
                        "0",
                        "0",
                        false
                    ),
                    new ToPlatform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "0")
                )
            );
    }

    /*
        Exceptions
     */

    @Test
    void shouldThrowApiValidationExceptionIfFromPlatformAndToPlatformAreTheSame() {
        var transferCryptoRequest = getTransferCryptoRequest(true);
        var userCryptoToTransfer = getUserCryptoToTransfer();
        var toPlatformResponse = getToPlatform();
        var fromPlatformResponse = getToPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatformResponse);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatformResponse);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);

        var exception = assertThrows(
            ApiValidationException.class,
            () -> transferCryptoService.transferCrypto(transferCryptoRequest)
        );

        assertThat(exception)
            .usingRecursiveComparison()
            .isEqualTo(new ApiValidationException(HttpStatus.BAD_REQUEST, SAME_FROM_TO_PLATFORM));
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionIfQuantityToTransferIsHigherThanAvailableQuantity() {
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("5"),
            new BigDecimal("0.0005"),
            false,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("2.375321283"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);

        var exception = assertThrows(
            InsufficientBalanceException.class,
            () -> transferCryptoService.transferCrypto(transferCryptoRequest)
        );

        assertEquals(NOT_ENOUGH_BALANCE, exception.getMessage());
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionIfNetworkFeeIsHigherThanAvailableQuantity() {
        var transferCryptoRequest = new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("0.5"),
            new BigDecimal("5"),
            false,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
        var userCryptoToTransfer = new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("2.375321283"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
        var toPlatform = getToPlatform();
        var fromPlatform = getFromPlatform();

        when(platformServiceMock.retrievePlatformById("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b")).thenReturn(toPlatform);
        when(platformServiceMock.retrievePlatformById("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9")).thenReturn(fromPlatform);
        when(userCryptoServiceMock.findUserCryptoById("f47ac10b-58cc-4372-a567-0e02b2c3d479")).thenReturn(userCryptoToTransfer);

        var exception = assertThrows(
            InsufficientBalanceException.class,
            () -> transferCryptoService.transferCrypto(transferCryptoRequest)
        );

        assertEquals(NOT_ENOUGH_BALANCE, exception.getMessage());
    }

    private TransferCryptoRequest getTransferCryptoRequest(Boolean sendFullQuantity) {
        return new TransferCryptoRequest(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("0.51"),
            new BigDecimal("0.0005"),
            sendFullQuantity,
            "b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b"
        );
    }

    private UserCrypto getUserCryptoToTransfer() {
        return new UserCrypto(
            "f47ac10b-58cc-4372-a567-0e02b2c3d479",
            new BigDecimal("2.375321283"),
            BYBIT_PLATFORM,
            getBitcoinCryptoEntity()
        );
    }

    private UserCrypto getToPlatformUserCrypto() {
        return new UserCrypto(
            "a6b9f1e8-c1d5-4a8b-bf52-836e6a2e4c3d",
            new BigDecimal("1.752438292"),
            BINANCE_PLATFORM,
            getBitcoinCryptoEntity()
        );
    }

    private Platform getToPlatform() {
        return new Platform("b8e8c277-e4b4-4b7e-9c5d-7885ef04b71b", "BINANCE");
    }

    private Platform getFromPlatform() {
        return new Platform("d5f63c4d-98e7-4d26-b380-e7d0f5c423e9", "BYBIT");
    }

}
