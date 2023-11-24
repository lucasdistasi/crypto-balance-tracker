package com.distasilucas.cryptobalancetracker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    private final String coingeckoApiKey;
    private final String coingeckoProUrl;
    private final String coingeckoUrl;

    public RestClientConfig(@Value("${coingecko.api-key}") String coingeckoApiKey,
                           @Value("${coingecko.pro.url}") String coingeckoProUrl,
                           @Value("${coingecko.url}") String coingeckoUrl) {
        this.coingeckoApiKey = coingeckoApiKey;
        this.coingeckoProUrl = coingeckoProUrl;
        this.coingeckoUrl = coingeckoUrl;
    }

    @Bean
    public RestClient coingeckoRestClient() {
        var baseUrl = StringUtils.hasText(coingeckoApiKey) ? coingeckoProUrl : coingeckoUrl;

        return RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(getSimpleClientHttpRequestFactory())
                .build();
    }

    private static SimpleClientHttpRequestFactory getSimpleClientHttpRequestFactory() {
        var simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(Duration.ofSeconds(5));
        simpleClientHttpRequestFactory.setReadTimeout(Duration.ofSeconds(10));
        simpleClientHttpRequestFactory.setChunkSize(36 * 1024);

        return simpleClientHttpRequestFactory;
    }
}
