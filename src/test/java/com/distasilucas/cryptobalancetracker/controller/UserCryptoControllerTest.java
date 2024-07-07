package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.request.usercrypto.TransferCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.FromPlatform;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.ToPlatform;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.TransferCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import com.distasilucas.cryptobalancetracker.service.TransferCryptoService;
import com.distasilucas.cryptobalancetracker.service.UserCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCryptoRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class UserCryptoControllerTest {

    @Mock
    private UserCryptoService userCryptoServiceMock;

    @Mock
    private TransferCryptoService transferCryptoServiceMock;

    private UserCryptoController userCryptoController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        userCryptoController = new UserCryptoController(userCryptoServiceMock, transferCryptoServiceMock);
    }

    @Test
    void shouldRetrieveUserCryptoByIdWithStatus200() {
        var userCrypto = getUserCrypto();
        var userCryptoResponse = userCrypto.toUserCryptoResponse();

        when(userCryptoServiceMock.findUserCryptoById("bitcoin")).thenReturn(userCrypto);

        var responseEntity = userCryptoController.retrieveUserCrypto("bitcoin");

        assertThat(responseEntity)
            .usingRecursiveAssertion()
            .isEqualTo(ResponseEntity.ok(userCryptoResponse));
    }

    @Test
    void shouldRetrieveUserCryptosForPageWithStatus200() {
        var userCrypto = getUserCrypto();
        var userCryptoResponse = userCrypto.toUserCryptoResponse();
        var pageUserCryptoResponse = new PageUserCryptoResponse(1, 1, false, List.of(userCryptoResponse));

        when(userCryptoServiceMock.retrieveUserCryptosByPage(0)).thenReturn(pageUserCryptoResponse);

        var responseEntity = userCryptoController.retrieveUserCryptosForPage(0);

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(pageUserCryptoResponse));
    }

    @Test
    void shouldRetrieveUserCryptosForPageWithStatus204() {
        var pageUserCryptoResponse = new PageUserCryptoResponse(1, 1, false, Collections.emptyList());

        when(userCryptoServiceMock.retrieveUserCryptosByPage(0)).thenReturn(pageUserCryptoResponse);

        var responseEntity = userCryptoController.retrieveUserCryptosForPage(0);

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }

    @Test
    void shouldRetrieveSavedUserCryptoWithStatus200() {
        var userCryptoRequest = getUserCryptoRequest();
        var userCryptoResponse = new UserCryptoResponse("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "bitcoin", "0.25", "COINBASE");

        when(userCryptoServiceMock.saveUserCrypto(userCryptoRequest)).thenReturn(userCryptoResponse);

        var responseEntity = userCryptoController.saveUserCrypto(userCryptoRequest);

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(userCryptoResponse));
    }

    @Test
    void shouldRetrieveUpdatedUserCryptoWithStatus200() {
        var userCryptoRequest = getUserCryptoRequest();
        var userCryptoResponse = new UserCryptoResponse("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "bitcoin", "0.25", "BINANCE");

        when(userCryptoServiceMock.updateUserCrypto("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", userCryptoRequest))
            .thenReturn(userCryptoResponse);

        var responseEntity = userCryptoController.updateUserCrypto("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", userCryptoRequest);

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(userCryptoResponse));
    }

    @Test
    void shouldReturnStatus200WhenDeletingUserCrypto() {
        doNothing().when(userCryptoServiceMock).deleteUserCrypto("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        var responseEntity = userCryptoController.deleteUserCrypto("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok().build());
    }

    @Test
    void shouldTransferCryptoAndReturn200() {
        var transferCryptoRequest = new TransferCryptoRequest(
            "9da7b110-8937-4c3a-82d2-bc1923a43278",
            new BigDecimal("0.2"),
            new BigDecimal("0.0005"),
            null,
            "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        );
        var transferCryptoResponse = new TransferCryptoResponse(getFromPlatform(), getToPlatform());

        when(transferCryptoServiceMock.transferCrypto(transferCryptoRequest)).thenReturn(transferCryptoResponse);

        var responseEntity = userCryptoController.transferUserCrypto(transferCryptoRequest);

        assertThat(responseEntity)
            .isEqualTo(ResponseEntity.ok(transferCryptoResponse));
    }

    private FromPlatform getFromPlatform() {
        return new FromPlatform(
            "9da7b110-8937-4c3a-82d2-bc1923a43278",
            "0.0005",
            "0.2",
            "0.1",
            "0.105",
            "0.1",
            true
        );
    }

    private ToPlatform getToPlatform() {
        return new ToPlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "1.15");
    }

}
