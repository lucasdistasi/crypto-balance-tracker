package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "PriceTargets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceTarget implements Serializable {

    @Id
    private String id;

    private BigDecimal target;

    @ManyToOne
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    public PriceTarget(BigDecimal target, Crypto crypto) {
        this.id = UUID.randomUUID().toString();
        this.target = target;
        this.crypto = crypto;
    }

    public PriceTargetResponse toPriceTargetResponse() {
        var change = calculateChangeNeeded();

        return new PriceTargetResponse(id, crypto.getCryptoInfo().getName(), crypto.getLastKnownPrices().getLastKnownPrice().toPlainString(), target.toPlainString(), change);
    }

    private float calculateChangeNeeded() {
        var currentPrice = crypto.getLastKnownPrices().getLastKnownPrice();
        var change = target.subtract(currentPrice)
            .divide(currentPrice, 3, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP);

        return isChangeNeededGreaterThanMaxFloat(change) ? 9999999F : change.floatValue();
    }

    private boolean isChangeNeededGreaterThanMaxFloat(BigDecimal change) {
        return change.compareTo(new BigDecimal("9999999")) > 0;
    }
}
