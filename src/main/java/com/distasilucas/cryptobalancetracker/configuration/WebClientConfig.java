package com.distasilucas.cryptobalancetracker.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

@Configuration
public class WebClientConfig {

    private final String coingeckoApiKey;
    private final String coingeckoProUrl;
    private final String coingeckoUrl;

    public WebClientConfig(@Value("${coingecko.api-key}") String coingeckoApiKey,
                           @Value("${coingecko.pro.url}") String coingeckoProUrl,
                           @Value("${coingecko.url}") String coingeckoUrl) {
        this.coingeckoApiKey = coingeckoApiKey;
        this.coingeckoProUrl = coingeckoProUrl;
        this.coingeckoUrl = coingeckoUrl;
    }

    @Bean
    public WebClient coingeckoWebClient() {
        var baseUrl = StringUtils.hasText(coingeckoApiKey) ? coingeckoProUrl : coingeckoUrl;
        var httpClient = HttpClient.create().responseTimeout(Duration.ofSeconds(10));
        var httpConnector = new ReactorClientHttpConnector(httpClient);

        return WebClient.builder()
                .codecs(clientCodecConfigurer -> clientCodecConfigurer.defaultCodecs().maxInMemorySize(700 * 1024))
                .baseUrl(baseUrl)
                .clientConnector(httpConnector)
                .build();
    }
}
