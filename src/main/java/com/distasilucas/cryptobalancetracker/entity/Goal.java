package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Entity
@Table(name = "Goals")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Goal implements Serializable {

    @Id
    private String id;

    @Column(name = "goal_quantity")
    private BigDecimal goalQuantity;

    @OneToOne
    @JoinColumn(name = "crypto_id", nullable = false, unique = true)
    private Crypto crypto;

    public Goal withNewGoalQuantity(BigDecimal newGoalQuantity) {
        return new Goal(this.id, newGoalQuantity, this.crypto);
    }

    public GoalResponse toGoalResponse(List<UserCrypto> userCryptos) {
        var actualQuantity = userCryptos
            .stream()
            .map(UserCrypto::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var progress = getProgress(actualQuantity);
        var remainingQuantity = getRemainingQuantity(goalQuantity, actualQuantity);
        var moneyNeeded = getMoneyNeeded(remainingQuantity);

        return new GoalResponse(id, crypto.getCryptoInfo().getName(), actualQuantity.toPlainString(), progress,
            remainingQuantity.toPlainString(), goalQuantity.toPlainString(), moneyNeeded.toPlainString());
    }

    private Float getProgress(BigDecimal actualQuantity) {
        return goalQuantity.compareTo(actualQuantity) <= 0 ? 100F :
            actualQuantity.multiply(new BigDecimal("100"))
                .divide(goalQuantity, 2, RoundingMode.HALF_UP)
                .floatValue();
    }

    private BigDecimal getRemainingQuantity(BigDecimal goalQuantity, BigDecimal actualQuantity) {
        return goalQuantity.compareTo(actualQuantity) <= 0 ? BigDecimal.ZERO : goalQuantity.subtract(actualQuantity);
    }

    private BigDecimal getMoneyNeeded(BigDecimal remainingQuantity) {
        return crypto.getLastKnownPrices().getLastKnownPrice().multiply(remainingQuantity).setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public String toString() {
        return String.format("Goal[id=%s, cryptoName=%s, goalQuantity=%s]", id, crypto.getCryptoInfo().getName(), goalQuantity);
    }
}
