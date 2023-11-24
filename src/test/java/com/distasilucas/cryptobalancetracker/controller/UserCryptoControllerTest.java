package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.service.UserCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

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

    private UserCryptoController userCryptoController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        userCryptoController = new UserCryptoController(userCryptoServiceMock);
    }

    @Test
    void shouldRetrieveUserCryptoByIdWithStatus200() {
        var userCrypto = getUserCrypto();
        var userCryptoResponse = userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE");

        when(userCryptoServiceMock.retrieveUserCryptoById("bitcoin")).thenReturn(userCryptoResponse);

        var responseEntity = userCryptoController.retrieveUserCrypto("bitcoin");

        assertThat(responseEntity)
                .usingRecursiveAssertion()
                .isEqualTo(ResponseEntity.ok(userCryptoResponse));
    }

    @Test
    void shouldRetrieveUserCryptosForPageWithStatus200() {
        var userCrypto = getUserCrypto();
        var userCryptoResponse = userCrypto.toUserCryptoResponse("Bitcoin", "COINBASE");
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
        var userCryptoEntity = userCryptoRequest.toEntity("bitcoin");
        var userCryptoResponse = userCryptoEntity.toUserCryptoResponse("bitcoin", "COINBASE");

        when(userCryptoServiceMock.saveUserCrypto(userCryptoRequest)).thenReturn(userCryptoResponse);

        var responseEntity = userCryptoController.saveUserCrypto(userCryptoRequest);

        assertThat(responseEntity)
                .isEqualTo(ResponseEntity.ok(userCryptoResponse));
    }

    @Test
    void shouldRetrieveUpdatedUserCryptoWithStatus200() {
        var userCryptoRequest = getUserCryptoRequest();
        var userCryptoEntity = userCryptoRequest.toEntity("bitcoin");
        var userCryptoResponse = userCryptoEntity.toUserCryptoResponse("bitcoin", "BINANCE");

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

}