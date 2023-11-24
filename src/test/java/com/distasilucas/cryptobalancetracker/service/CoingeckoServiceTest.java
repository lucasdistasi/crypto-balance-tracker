package com.distasilucas.cryptobalancetracker.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoingeckoServiceTest {

    public static final String COINGECKO_API_URL = "https://api.coingecko.com/api/v3";

    private CoingeckoService coingeckoService;
    private static MockWebServer mockWebServer;

    @BeforeAll
    static void setUp() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    void shouldRetrieveAllCryptos() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);

        var webClient = WebClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("", webClient);

        var cryptos = coingeckoService.retrieveAllCryptos();

        StepVerifier.create(Flux.just(cryptos))
                .expectNextMatches(coingeckoCryptos -> !coingeckoCryptos.isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldRetrieveCoingeckoCryptoInfo() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);

        var webClient = WebClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("", webClient);

        var coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo("bitcoin");

        StepVerifier.create(Mono.just(coingeckoCryptoInfo))
                .expectNextMatches(coingeckoCrypto -> coingeckoCrypto.id().equalsIgnoreCase("bitcoin"))
                .verifyComplete();
    }

    @Test
    void shouldThrowWebClientResponseExceptionWithNotFoundCodeWhenSearchingForNonExistentCrypto() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);

        var webClient = WebClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("", webClient);

        var exception = assertThrows(
                WebClientResponseException.class,
                () -> coingeckoService.retrieveCryptoInfo("pipicoin")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowWebClientResponseExceptionWithBadRequestWhenUsingInvalidApiKeyForCryptoInfo() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);

        var webClient = WebClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("TEST123", webClient);

        var exception = assertThrows(
                WebClientResponseException.class,
                () -> coingeckoService.retrieveCryptoInfo("bitcoin")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldThrowWebClientResponseExceptionWithBadRequestWhenUsingInvalidApiKeyForAllCryptos() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);

        var webClient = WebClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("TEST123", webClient);

        var exception = assertThrows(
                WebClientResponseException.class,
                () -> coingeckoService.retrieveAllCryptos()
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

}