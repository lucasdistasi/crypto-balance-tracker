package com.distasilucas.cryptobalancetracker.model.response.insights.crypto;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;

import java.io.Serializable;
import java.util.List;

public record CryptoInsightResponse(
    String cryptoName,
    BalancesResponse balances,
    List<PlatformInsight> platforms
) implements Serializable {
}
