package com.distasilucas.cryptobalancetracker.model.response.goal;

public record GoalResponse(
    String id,
    String cryptoName,
    String actualQuantity,
    float progress,
    String remainingQuantity,
    String goalQuantity,
    String moneyNeeded
) {
}
