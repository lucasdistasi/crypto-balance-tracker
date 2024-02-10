package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.util.List;

public record UserCryptosInsights(
    CryptoInfo cryptoInfo,
    String quantity,
    float percentage,
    BalancesResponse balances,
    int marketCapRank,
    MarketData marketData,
    List<String> platforms
) {
}
