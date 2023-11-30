package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.util.List;

public record UserCryptosInsights(
        CryptoInfo cryptoInfo,
        String quantity,
        float percentage,
        BalancesResponse balances,
        MarketData marketData,
        List<String> platforms
) {
}
