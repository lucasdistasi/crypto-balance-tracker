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
public class ChangePercentages implements Serializable {

    @Column(name = "change_percentage_in_24h")
    private BigDecimal changePercentageIn24h;

    @Column(name = "change_percentage_in_7d")
    private BigDecimal changePercentageIn7d;

    @Column(name = "change_percentage_in_30d")
    private BigDecimal changePercentageIn30d;

    public ChangePercentages(MarketData marketData) {
        this.changePercentageIn24h = marketData.changePercentageIn24h();
        this.changePercentageIn7d = marketData.changePercentageIn7d();
        this.changePercentageIn30d = marketData.changePercentageIn30d();
    }
}
