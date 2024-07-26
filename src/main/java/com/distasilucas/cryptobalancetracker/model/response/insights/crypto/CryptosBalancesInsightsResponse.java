package com.distasilucas.cryptobalancetracker.model.response.insights.crypto;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;

import java.io.Serializable;
import java.util.List;

public record CryptosBalancesInsightsResponse(
    BalancesResponse balances,
    List<CryptoInsights> cryptos
) implements Serializable {
}
