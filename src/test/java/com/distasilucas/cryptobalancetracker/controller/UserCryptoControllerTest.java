package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.service.UserCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;
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

}