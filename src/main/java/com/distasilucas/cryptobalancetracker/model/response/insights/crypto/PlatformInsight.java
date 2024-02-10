package com.distasilucas.cryptobalancetracker.model.response.insights.crypto;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;

public record PlatformInsight(
    String quantity,
    BalancesResponse balances,
    float percentage,
    String platformName
) {
}
