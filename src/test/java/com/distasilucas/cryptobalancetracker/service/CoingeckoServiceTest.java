package com.distasilucas.cryptobalancetracker.service;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        var restClient = RestClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("", restClient);

        var cryptos = coingeckoService.retrieveAllCryptos();

        assertFalse(cryptos.isEmpty());
    }

    @Test
    void shouldRetrieveCoingeckoCryptoInfo() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);
        var restClient = RestClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("", restClient);

        var coingeckoCryptoInfo = coingeckoService.retrieveCryptoInfo("bitcoin");

        assertEquals("bitcoin", coingeckoCryptoInfo.id());
    }

    @Test
    void shouldThrowRestClientResponseExceptionWithNotFoundCodeWhenSearchingForNonExistentCrypto() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);
        var restClient = RestClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("", restClient);

        var exception = assertThrows(
                RestClientResponseException.class,
                () -> coingeckoService.retrieveCryptoInfo("pipicoin")
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void shouldThrowRestClientResponseExceptionWithBadRequestWhenUsingInvalidApiKeyForCryptoInfo() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);
        var restClient = RestClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("TEST123", restClient);

        var exception = assertThrows(
                RestClientResponseException.class,
                () -> coingeckoService.retrieveCryptoInfo("bitcoin")
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void shouldThrowRestClientResponseExceptionWithBadRequestWhenUsingInvalidApiKeyForAllCryptos() {
        var mockResponse = new MockResponse();
        mockWebServer.enqueue(mockResponse);
        var restClient = RestClient.create(COINGECKO_API_URL);
        coingeckoService = new CoingeckoService("TEST123", restClient);

        var exception = assertThrows(
                RestClientResponseException.class,
                () -> coingeckoService.retrieveAllCryptos()
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

}