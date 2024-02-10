package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.UUID;

@Document("Goals")
public record Goal(
    @Id
    String id,

    @Field("crypto_id")
    String coingeckoCryptoId,

    @Field("goal_quantity")
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
