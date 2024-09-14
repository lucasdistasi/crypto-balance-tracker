package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.distasilucas.cryptobalancetracker.entity.Balances;

import java.io.Serializable;

public record BalancesResponse(
    String totalUSDBalance,
    String totalEURBalance,
    String totalBTCBalance
) implements Serializable {

    public BalancesResponse(Balances balances) {
        this(balances.getUsdBalance(), balances.getEurBalance(), balances.getBtcBalance());
    }

    public static BalancesResponse empty() {
        return new BalancesResponse("0", "0", "0");
    }
}
