package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

public record MarketData(

    @JsonProperty("current_price")
    CurrentPrice currentPrice,

    @JsonProperty("circulating_supply")
    BigDecimal circulatingSupply,

    @JsonProperty("max_supply")
    BigDecimal maxSupply,

    @JsonProperty("market_cap")
    MarketCap marketCap,

    @JsonProperty("price_change_percentage_24h")
    BigDecimal changePercentageIn24h,

    @JsonProperty("price_change_percentage_7d")
    BigDecimal changePercentageIn7d,

    @JsonProperty("price_change_percentage_30d")
    BigDecimal changePercentageIn30d
) implements Serializable {

    public BigDecimal changePercentageIn24h() {
        return changePercentageIn24h.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal changePercentageIn7d() {
        return changePercentageIn7d.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal changePercentageIn30d() {
        return changePercentageIn30d.setScale(2, RoundingMode.HALF_UP);
    }
}
