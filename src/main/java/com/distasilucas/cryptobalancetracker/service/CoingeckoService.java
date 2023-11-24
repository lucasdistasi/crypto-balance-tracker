package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class CoingeckoService {

    private static final String COIN_URI = "/coins/";
    private static final String COINS_URI = COIN_URI + "/list";

    private final String coingeckoApiKey;
    private final WebClient coingeckoWebClient;

    public CoingeckoService(@Value("${coingecko.api-key}") String coingeckoApiKey, WebClient coingeckoWebClient) {
        this.coingeckoApiKey = coingeckoApiKey;
        this.coingeckoWebClient = coingeckoWebClient;
    }

    @Retryable(retryFor = WebClientException.class, backoff = @Backoff(delay = 1500))
    public List<CoingeckoCrypto> retrieveAllCryptos() {
        log.info("Hitting Coingecko API... Retrieving all cryptos...");

        return coingeckoWebClient.get()
                .uri(getCryptosURI())
                .retrieve()
                .bodyToFlux(CoingeckoCrypto.class)
                .collectList()
                .block();
    }

    @Retryable(retryFor = WebClientException.class, backoff = @Backoff(delay = 1500))
    public CoingeckoCryptoInfo retrieveCryptoInfo(String coingeckoCryptoId) {
        log.info("Hitting Coingecko API... Retrieving information for {}...", coingeckoCryptoId);
        var coinURI = COIN_URI.concat(coingeckoCryptoId);

        return coingeckoWebClient.get()
                .uri(getCoingeckoCryptoInfoURI(coinURI))
                .retrieve()
                .bodyToMono(CoingeckoCryptoInfo.class)
                .block();
    }

    private Function<UriBuilder, URI> getCryptosURI() {
        Function<UriBuilder, URI> proCoingeckoURI = uriBuilder -> uriBuilder.path(COINS_URI)
                .queryParam("x_cg_pro_api_key", coingeckoApiKey)
                .build();

        Function<UriBuilder, URI> freeCoingeckoURI = uriBuilder -> uriBuilder.path(COINS_URI).build();

        return StringUtils.hasText(coingeckoApiKey) ? proCoingeckoURI : freeCoingeckoURI;
    }

    private Function<UriBuilder, URI> getCoingeckoCryptoInfoURI(String url) {
        MultiValueMap<String, String> commonParams = new HttpHeaders();
        commonParams.add("tickers", "false");
        commonParams.add("community_data", "false");
        commonParams.add("developer_data", "false");

        Function<UriBuilder, URI> proCoingeckoURI = uriBuilder -> uriBuilder.path(url)
                .queryParam("x_cg_pro_api_key", coingeckoApiKey)
                .queryParams(commonParams)
                .build();

        Function<UriBuilder, URI> freeCoingeckoURI = uriBuilder -> uriBuilder.path(url)
                .queryParams(commonParams)
                .build();

        return StringUtils.hasText(coingeckoApiKey) ? proCoingeckoURI : freeCoingeckoURI;
    }
}
