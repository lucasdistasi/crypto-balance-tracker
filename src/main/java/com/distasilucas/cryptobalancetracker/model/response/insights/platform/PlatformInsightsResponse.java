package com.distasilucas.cryptobalancetracker.model.response.insights.platform;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record PlatformInsightsResponse(
    String platformName,
    BalancesResponse balances,
    List<CryptoInsights> cryptos
) implements Serializable {

    public static PlatformInsightsResponse empty() {
        return new PlatformInsightsResponse(null, BalancesResponse.empty(), Collections.emptyList());
    }
}
