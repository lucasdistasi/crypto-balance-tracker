package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "Goals")
public record Goal(
    @Id
    String id,

    @Column(name = "crypto_id")
    String coingeckoCryptoId,

    @Column(name = "goal_quantity")
    BigDecimal goalQuantity
) {

    public Goal(String coingeckoCryptoId, BigDecimal goalQuantity) {
        this(UUID.randomUUID().toString(), coingeckoCryptoId, goalQuantity);
    }

    public GoalResponse toGoalResponse(String id, String cryptoName, BigDecimal actualQuantity, float progress,
                                       BigDecimal remainingQuantity, BigDecimal moneyNeeded) {
        return new GoalResponse(id, cryptoName, actualQuantity.toPlainString(), progress,
            remainingQuantity.toPlainString(), goalQuantity.toPlainString(), moneyNeeded.toPlainString());
    }
}
