package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class CryptoInfo implements Serializable {
    private String name;
    private String ticker;
    private String image;

    @Column(name = "market_cap_rank")
    private int marketCapRank;

    @Column(name = "market_cap")
    private BigDecimal marketCap;

    @Column(name = "circulating_supply")
    private BigDecimal circulatingSupply;

    @Column(name = "max_supply")
    private BigDecimal maxSupply;

    public CryptoInfo(CoingeckoCryptoInfo coingeckoCryptoInfo) {
        var marketData = coingeckoCryptoInfo.marketData();

        this.name = coingeckoCryptoInfo.name();
        this.ticker = coingeckoCryptoInfo.symbol();
        this.image = coingeckoCryptoInfo.image().large();
        this.marketCapRank = coingeckoCryptoInfo.marketCapRank();
        this.marketCap = marketData.marketCap().usd();
        this.circulatingSupply = marketData.circulatingSupply();
        this.maxSupply = marketData.maxSupply() != null ? marketData.maxSupply() : BigDecimal.ZERO;
    }
}
