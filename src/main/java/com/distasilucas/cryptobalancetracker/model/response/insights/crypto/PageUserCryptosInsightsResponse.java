package com.distasilucas.cryptobalancetracker.model.response.insights.crypto;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights;

import java.util.List;

public record PageUserCryptosInsightsResponse(
    int page,
    int totalPages,
    boolean hasNextPage,
    BalancesResponse balances,
    List<UserCryptosInsights> cryptos
) {

    public PageUserCryptosInsightsResponse(int page, int totalPages, BalancesResponse balancesResponse, List<UserCryptosInsights> cryptos) {
        this(page + 1, totalPages, totalPages - 1 > page, balancesResponse, cryptos);
    }
}
