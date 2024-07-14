package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.service.PlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PlatformControllerTest {

    @Mock
    private PlatformService platformServiceMock;

    private PlatformController platformController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        platformController = new PlatformController(platformServiceMock);
    }

    @Test
    void shouldRetrieveAllPlatformsWithStatus200() {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var platformResponse = platformEntity.toPlatformResponse();

        when(platformServiceMock.retrieveAllPlatforms()).thenReturn(List.of(platformEntity));

        var responseEntity = platformController.retrieveAllPlatforms();

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(List.of(platformResponse)));
    }

    @Test
    void shouldRetrieveAllPlatformsWithStatus204() {
        when(platformServiceMock.retrieveAllPlatforms()).thenReturn(Collections.emptyList());

        var responseEntity = platformController.retrieveAllPlatforms();

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldRetrievePlatformByIdWithStatus200() {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var platformResponse = platformEntity.toPlatformResponse();

        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
            .thenReturn(platformEntity);

        var responseEntity = platformController.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformResponse));
    }

    @Test
    void shouldRetrieveSavedPlatformWithStatus200() {
        var platformRequest = new PlatformRequest("binance");
        var platformEntity = platformRequest.toEntity();
        var platformResponse = platformEntity.toPlatformResponse();

        when(platformServiceMock.savePlatform(platformRequest)).thenReturn(platformEntity);

        var responseEntity = platformController.savePlatform(platformRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformResponse));
    }

    @Test
    void shouldRetrieveUpdatedPlatformWithStatus200() {
        var platformRequest = new PlatformRequest("bybit");
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BYBIT");
        var platformResponse = platformEntity.toPlatformResponse();

        when(platformServiceMock.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest))
            .thenReturn(platformEntity);

        var responseEntity = platformController.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(platformResponse));
    }

    @Test
    void shouldReturnStatus200WhenDeletingPlatform() {
        doNothing().when(platformServiceMock).deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        var responseEntity = platformController.deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

}
