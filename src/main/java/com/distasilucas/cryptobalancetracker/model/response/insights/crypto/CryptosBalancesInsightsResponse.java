package com.distasilucas.cryptobalancetracker.model.response.insights.crypto;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record CryptosBalancesInsightsResponse(
    BalancesResponse balances,
    List<CryptoInsights> cryptos
) implements Serializable {

    public static CryptosBalancesInsightsResponse empty() {
        var emptyBalances = new BalancesResponse("0", "0", "0");

        return new CryptosBalancesInsightsResponse(emptyBalances, Collections.emptyList());
    }
}
