package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import com.distasilucas.cryptobalancetracker.service.PriceTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class PriceTargetControllerTest {

    @Mock
    private PriceTargetService priceTargetServiceMock;

    private PriceTargetController priceTargetController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        priceTargetController = new PriceTargetController(priceTargetServiceMock);
    }

    @Test
    void shouldRetrievePriceTargetWithStatus200() {
        var priceTargetResponse = new PriceTargetResponse(
            "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
            "Bitcoin",
            "60000",
            "100000",
            35.30F
        );

        when(priceTargetServiceMock.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08"))
            .thenReturn(priceTargetResponse);

        var responseEntity = priceTargetController.retrievePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(priceTargetResponse));
    }

    @Test
    void shouldRetrievePriceTargetsForPageWithStatus200() {
        var pagePriceTargetResponse = new PagePriceTargetResponse(
            0,
            1,
            List.of(
                new PriceTargetResponse(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    "Bitcoin",
                    "60000",
                    "100000",
                    35.30F
                )
            )
        );

        when(priceTargetServiceMock.retrievePriceTargetsByPage(0)).thenReturn(pagePriceTargetResponse);

        var responseEntity = priceTargetController.retrievePriceTargetsByPage(0);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(pagePriceTargetResponse));
    }

    @Test
    void shouldRetrievePriceTargetsForPageWithNextPageWithStatus200() {
        var pagePriceTargetResponse = new PagePriceTargetResponse(
            0,
            2,
            List.of(
                new PriceTargetResponse(
                    "f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08",
                    "Bitcoin",
                    "60000",
                    "100000",
                    35.30F
                )
            )
        );

        when(priceTargetServiceMock.retrievePriceTargetsByPage(0)).thenReturn(pagePriceTargetResponse);

        var responseEntity = priceTargetController.retrievePriceTargetsByPage(0);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(pagePriceTargetResponse));
    }

    @Test
    void shouldRetrieveEmptyPriceTargetsForPageWithStatus204() {
        var pagePriceTargetResponse = new PagePriceTargetResponse(0, 1, emptyList());

        when(priceTargetServiceMock.retrievePriceTargetsByPage(0)).thenReturn(pagePriceTargetResponse);

        var responseEntity = priceTargetController.retrievePriceTargetsByPage(0);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldSavePriceTargetWithStatus201() {
        var priceTargetRequest = new PriceTargetRequest("Bitcoin", new BigDecimal("120000"));
        var priceTargetResponse = new PriceTargetResponse("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "Bitcoin", "60000", "120000", 35F);

        when(priceTargetServiceMock.savePriceTarget(priceTargetRequest)).thenReturn(priceTargetResponse);

        var responseEntity = priceTargetController.savePriceTarget(priceTargetRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.status(HttpStatus.CREATED).body(priceTargetResponse));
    }

    @Test
    void shouldUpdatePriceTargetWithStatus200() {
        var priceTargetRequest = new PriceTargetRequest("Bitcoin", new BigDecimal("150000"));
        var priceTargetResponse = new PriceTargetResponse("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", "Bitcoin", "60000", "150000", 40F);

        when(priceTargetServiceMock.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest))
            .thenReturn(priceTargetResponse);

        var responseEntity = priceTargetController.updatePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08", priceTargetRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(priceTargetResponse));
    }

    @Test
    void shouldDeletePriceTargetWithStatus204() {
        var responseEntity = priceTargetController.deletePriceTarget("f9c8cb17-73a4-4b7e-96f6-7943e3ddcd08");

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }
}
