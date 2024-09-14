package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.coingecko.MarketData;
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
public class LastKnownPrices implements Serializable {

    @Column(name = "last_known_price")
    private BigDecimal lastKnownPrice;

    @Column(name = "last_known_price_in_eur")
    private BigDecimal lastKnownPriceInEUR;

    @Column(name = "last_known_price_in_btc")
    private BigDecimal lastKnownPriceInBTC;

    public LastKnownPrices(MarketData marketData) {
        this.lastKnownPrice = marketData.currentPrice().usd();
        this.lastKnownPriceInEUR = marketData.currentPrice().eur();
        this.lastKnownPriceInBTC = marketData.currentPrice().btc();
    }
}
