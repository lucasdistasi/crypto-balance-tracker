package com.distasilucas.cryptobalancetracker.model.response.insights.platform;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;

import java.util.List;

public record PlatformInsightsResponse(
        String platformName,
        BalancesResponse balances,
        List<CryptoInsights> cryptos
) {
}
