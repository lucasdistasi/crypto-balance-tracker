package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Entity
@Table(name = "PriceTargets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PriceTarget {

    @Id
    private String id;

    @Column(name = "crypto_id")
    private String coingeckoCryptoId;

    private BigDecimal target;

    public PriceTarget(String coingeckoCryptoId, BigDecimal target) {
        this(UUID.randomUUID().toString(), coingeckoCryptoId, target);
    }

    public PriceTargetResponse toPriceTargetResponse(String cryptoName, BigDecimal currentPrice, float change) {
        return new PriceTargetResponse(id, cryptoName, currentPrice.toPlainString(), target.toPlainString(), change);
    }

    public float calculateChangeNeeded(BigDecimal currentPrice) {
        return target.subtract(currentPrice)
            .divide(currentPrice, 3, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP)
            .floatValue();
    }
}
