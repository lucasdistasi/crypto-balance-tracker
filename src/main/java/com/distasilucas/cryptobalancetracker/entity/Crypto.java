package com.distasilucas.cryptobalancetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

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

    @OneToMany(mappedBy = "crypto")
    private List<UserCrypto> userCryptos;

    public Crypto(String id, String name, String ticker, String image, BigDecimal lastKnownPrice,
                  BigDecimal lastKnownPriceInEUR, BigDecimal lastKnownPriceInBTC, BigDecimal circulatingSupply,
                  BigDecimal maxSupply, int marketCapRank, BigDecimal marketCap, BigDecimal changePercentageIn24h,
                  BigDecimal changePercentageIn7d, BigDecimal changePercentageIn30d, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.name = name;
        this.ticker = ticker;
        this.image = image;
        this.lastKnownPrice = lastKnownPrice;
        this.lastKnownPriceInEUR = lastKnownPriceInEUR;
        this.lastKnownPriceInBTC = lastKnownPriceInBTC;
        this.circulatingSupply = circulatingSupply;
        this.maxSupply = maxSupply;
        this.marketCapRank = marketCapRank;
        this.marketCap = marketCap;
        this.changePercentageIn24h = changePercentageIn24h;
        this.changePercentageIn7d = changePercentageIn7d;
        this.changePercentageIn30d = changePercentageIn30d;
        this.lastUpdatedAt = lastUpdatedAt;
        this.userCryptos = Collections.emptyList();
    }
}
