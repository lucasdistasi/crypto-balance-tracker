package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.function.Function;

import static com.distasilucas.cryptobalancetracker.constants.Constants.COINGECKO_CRYPTOS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_INFO_CACHE;

@Slf4j
@Service
public class CoingeckoService {

    private static final String COIN_URI = "/coins/";
    private static final String COINS_URI = COIN_URI + "/list";
    private static final String DEMO_API_KEY_QUERY_PARAM = "x_cg_demo_api_key";
    private static final String PRO_API_KEY_QUERY_PARAM = "x_cg_pro_api_key";

    private final String proCoingeckoApiKey;
    private final String demoCoingeckoApiKey;
    private final RestClient coingeckoRestClient;

    public CoingeckoService(@Value("${coingecko.api-key.pro}") String proCoingeckoApiKey,
                            @Value("${coingecko.api-key.demo}") String demoCoingeckoApiKey,
                            RestClient coingeckoRestClient) {
        this.proCoingeckoApiKey = proCoingeckoApiKey;
        this.demoCoingeckoApiKey = demoCoingeckoApiKey;
        this.coingeckoRestClient = coingeckoRestClient;
    }

    @Cacheable(cacheNames = COINGECKO_CRYPTOS_CACHE)
    @Retryable(retryFor = RestClientException.class, backoff = @Backoff(delay = 1500))
    public List<CoingeckoCrypto> retrieveAllCryptos() {
        var coingeckoCryptosURI = getCryptosURI();
        var uriAsString = coingeckoCryptosURI.apply(UriComponentsBuilder.newInstance());
        log.info("Hitting Coingecko API for URI [{}] Retrieving all cryptos.", uriAsString);

        return coingeckoRestClient.get()
            .uri(coingeckoCryptosURI)
            .retrieve()
            .body(new ParameterizedTypeReference<>() {});
    }

    @Cacheable(cacheNames = CRYPTO_INFO_CACHE, key = "#coingeckoCryptoId")
    @Retryable(retryFor = RestClientException.class, backoff = @Backoff(delay = 1500))
    public CoingeckoCryptoInfo retrieveCryptoInfo(String coingeckoCryptoId) {
        var coinURI = COIN_URI.concat(coingeckoCryptoId);
        var coingeckoCryptoInfoURI = getCoingeckoCryptoInfoURI(coinURI);
        var uriAsString = coingeckoCryptoInfoURI.apply(UriComponentsBuilder.newInstance());
        log.info("Hitting Coingecko API for URI [{}] Retrieving information for {}.", uriAsString, coingeckoCryptoId);

        return coingeckoRestClient.get()
            .uri(coingeckoCryptoInfoURI)
            .retrieve()
            .body(CoingeckoCryptoInfo.class);
    }

    private Function<UriBuilder, URI> getCryptosURI() {
        Function<UriBuilder, URI> proCoingeckoURI = uriBuilder -> uriBuilder.path(COINS_URI)
            .queryParam(PRO_API_KEY_QUERY_PARAM, proCoingeckoApiKey)
            .build();

        Function<UriBuilder, URI> freeCoingeckoURI = uriBuilder -> uriBuilder.path(COINS_URI)
            .queryParam(DEMO_API_KEY_QUERY_PARAM, demoCoingeckoApiKey)
            .build();

        return StringUtils.hasText(proCoingeckoApiKey) ? proCoingeckoURI : freeCoingeckoURI;
    }

    private Function<UriBuilder, URI> getCoingeckoCryptoInfoURI(String url) {
        MultiValueMap<String, String> commonParams = new HttpHeaders();
        commonParams.add("tickers", "false");
        commonParams.add("community_data", "false");
        commonParams.add("developer_data", "false");
        commonParams.add("localization", "false");

        Function<UriBuilder, URI> proCoingeckoURI = uriBuilder -> uriBuilder.path(url)
            .queryParam(PRO_API_KEY_QUERY_PARAM, proCoingeckoApiKey)
            .queryParams(commonParams)
            .build();

        Function<UriBuilder, URI> freeCoingeckoURI = uriBuilder -> uriBuilder.path(url)
            .queryParams(commonParams)
            .queryParam(DEMO_API_KEY_QUERY_PARAM, demoCoingeckoApiKey)
            .build();

        return StringUtils.hasText(proCoingeckoApiKey) ? proCoingeckoURI : freeCoingeckoURI;
    }
}
