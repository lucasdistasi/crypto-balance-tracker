package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;

public record MarketData(

        @JsonProperty("current_price")
        CurrentPrice currentPrice,

        @JsonProperty("circulating_supply")
        BigDecimal circulatingSupply,

        @JsonProperty("max_supply")
        BigDecimal maxSupply
) implements Serializable {
}
