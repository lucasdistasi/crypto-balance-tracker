package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

public record CoingeckoCryptoInfo(
    String id,
    String symbol,
    String name,
    Image image,

    @JsonProperty("market_cap_rank")
    int marketCapRank,

    @JsonProperty("market_data")
    MarketData marketData
) implements Serializable {
}
