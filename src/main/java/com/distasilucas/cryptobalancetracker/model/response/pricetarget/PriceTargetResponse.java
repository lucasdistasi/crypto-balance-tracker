package com.distasilucas.cryptobalancetracker.model.response.pricetarget;

public record PriceTargetResponse(
    String priceTargetId,
    String cryptoName,
    String currentPrice,
    String priceTarget,
    float change
) {
}
