package com.distasilucas.cryptobalancetracker.model.response.insights;

public record BalancesResponse(
    String totalUSDBalance,
    String totalEURBalance,
    String totalBTCBalance
) {
}
