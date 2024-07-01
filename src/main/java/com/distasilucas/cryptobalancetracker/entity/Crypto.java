package com.distasilucas.cryptobalancetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "Cryptos")
@NoArgsConstructor
@AllArgsConstructor
public class Crypto implements Serializable {

    @Id
    private String id;
    private String name;
    private String ticker;
    private String image;

    @Column(name = "last_known_price")
    private BigDecimal lastKnownPrice;

    @Column(name = "last_known_price_in_eur")
    private BigDecimal lastKnownPriceInEUR;

    @Column(name = "last_known_price_in_btc")
    private BigDecimal lastKnownPriceInBTC;

    @Column(name = "circulating_supply")
    private BigDecimal circulatingSupply;

    @Column(name = "max_supply")
    private BigDecimal maxSupply;

    @Column(name = "market_cap_rank")
    private int marketCapRank;

    @Column(name = "market_cap")
    private BigDecimal marketCap;

    @Column(name = "change_percentage_in_24h")
    private BigDecimal changePercentageIn24h;

    @Column(name = "change_percentage_in_7d")
    private BigDecimal changePercentageIn7d;

    @Column(name = "change_percentage_in_30d")
    private BigDecimal changePercentageIn30d;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;
}
