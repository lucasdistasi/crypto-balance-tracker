package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Document("PriceTargets")
public record PriceTarget(
    @Id
    String id,

    @Field("crypto_id")
    String coingeckoCryptoId,

    BigDecimal target
) {

    public PriceTarget withTarget(BigDecimal target) {
        return new PriceTarget(id(), coingeckoCryptoId(), target);
    }

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
