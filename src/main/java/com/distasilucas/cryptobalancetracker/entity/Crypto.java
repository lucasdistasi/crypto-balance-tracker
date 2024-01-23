package com.distasilucas.cryptobalancetracker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document("Cryptos")
public record Crypto(
        @Id
        String id,
        String name,
        String ticker,
        String image,

        @Field("last_known_price")
        BigDecimal lastKnownPrice,

        @Field("last_known_price_in_eur")
        BigDecimal lastKnownPriceInEUR,

        @Field("last_known_price_in_btc")
        BigDecimal lastKnownPriceInBTC,

        @Field("circulating_supply")
        BigDecimal circulatingSupply,

        @Field("max_supply")
        BigDecimal maxSupply,

        @Field("market_cap_rank")
        int marketCapRank,

        @Field("market_cap")
        BigDecimal marketCap,

        @Field("change_percentage_in_24h")
        BigDecimal changePercentageIn24h,

        @Field("change_percentage_in_7d")
        BigDecimal changePercentageIn7d,

        @Field("change_percentage_in_30d")
        BigDecimal changePercentageIn30d,

        @Field("last_updated_at")
        LocalDateTime lastUpdatedAt
) implements Serializable {
}
