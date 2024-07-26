package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CryptoInsights(
    String id,
    String cryptoName,
    String cryptoId,
    String quantity,
    BalancesResponse balances,
    float percentage
) implements Serializable {

    public CryptoInsights(String cryptoName, BalancesResponse balancesResponse, float percentage) {
        this(null, cryptoName, null, null, balancesResponse, percentage);
    }

    public CryptoInsights(String cryptoName, String cryptoId, String quantity, BalancesResponse balancesResponse, float percentage) {
        this(null, cryptoName, cryptoId, quantity, balancesResponse, percentage);
    }
}
