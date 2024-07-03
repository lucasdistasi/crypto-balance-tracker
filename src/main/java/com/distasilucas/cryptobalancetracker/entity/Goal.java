package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "Goals")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Goal {

    @Id
    private String id;

    @Column(name = "goal_quantity")
    private BigDecimal goalQuantity;

    @ManyToOne
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    public GoalResponse toGoalResponse(String id, String cryptoName, BigDecimal actualQuantity, float progress,
                                       BigDecimal remainingQuantity, BigDecimal moneyNeeded) {
        return new GoalResponse(id, cryptoName, actualQuantity.toPlainString(), progress,
            remainingQuantity.toPlainString(), goalQuantity.toPlainString(), moneyNeeded.toPlainString());
    }
}
