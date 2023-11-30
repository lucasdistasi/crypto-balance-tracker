package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CryptoInsights(
        String cryptoName,
        String cryptoId,
        String quantity,
        BalancesResponse balances,
        float percentage
) {

    public CryptoInsights(String cryptoName, BalancesResponse balancesResponse, float percentage) {
        this(cryptoName, null, null, balancesResponse, percentage);
    }
}
