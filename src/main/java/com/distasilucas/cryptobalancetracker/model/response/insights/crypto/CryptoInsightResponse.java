package com.distasilucas.cryptobalancetracker.model.response.insights.crypto;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record CryptoInsightResponse(
    String cryptoName,
    BalancesResponse balances,
    List<PlatformInsight> platforms
) implements Serializable {

    public static CryptoInsightResponse empty() {
        return new CryptoInsightResponse(null, BalancesResponse.empty(), Collections.emptyList());
    }
}
