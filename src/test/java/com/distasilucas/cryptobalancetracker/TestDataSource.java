package com.distasilucas.cryptobalancetracker;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCrypto;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.CurrentPrice;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.Image;
import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.LocalDateTime;

import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_ENDPOINT;

public class TestDataSource {

    public static MockHttpServletRequestBuilder retrieveAllPlatforms() {
        return MockMvcRequestBuilders.get(PLATFORMS_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder retrievePlatformById(String platformId) {
        var url = PLATFORMS_ENDPOINT.concat("/%s".formatted(platformId));

        return MockMvcRequestBuilders.get(url)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder savePlatform(String content) {
        return MockMvcRequestBuilders.post(PLATFORMS_ENDPOINT)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder updatePlatform(String platformId, String content) {
        var url = PLATFORMS_ENDPOINT.concat("/%s".formatted(platformId));

        return MockMvcRequestBuilders.put(url)
                .content(content)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static MockHttpServletRequestBuilder deletePlatform(String platformId) {
        var url = PLATFORMS_ENDPOINT.concat("/%s".formatted(platformId));

        return MockMvcRequestBuilders.delete(url)
                .contentType(MediaType.APPLICATION_JSON);
    }

    public static String getFileContent(String path) throws IOException {
        var classPathResource = new ClassPathResource(path);
        return StreamUtils.copyToString(classPathResource.getInputStream(), Charset.defaultCharset());
    }

    public static Crypto getCryptoEntity() {
        return new Crypto(
                "bitcoin",
                "Bitcoin",
                "btc",
                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579",
                new BigDecimal("30000"),
                new BigDecimal("27000"),
                new BigDecimal("1"),
                new BigDecimal("19000000"),
                new BigDecimal("21000000"),
                LocalDateTime.of(2023, 1, 1, 0, 0, 0)
        );
    }

    public static CoingeckoCryptoInfo getCoingeckoCryptoInfo() {
        var image = new Image("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579");
        var currentPrice = new CurrentPrice(new BigDecimal("30000"), new BigDecimal("27000"), new BigDecimal("1"));
        var marketData = new MarketData(currentPrice, new BigDecimal("19000000"), new BigDecimal("21000000"));

        return new CoingeckoCryptoInfo("bitcoin", "btc", "Bitcoin", image, marketData);
    }

    public static CoingeckoCrypto getCoingeckoCrypto() {
        return new CoingeckoCrypto("bitcoin", "btc", "Bitcoin");
    }

    public static UserCrypto getUserCrypto() {
        return new UserCrypto(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "bitcoin",
                new BigDecimal("0.25"),
                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        );
    }
}
