package com.distasilucas.cryptobalancetracker.model.response.insights.platform;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;

import java.util.List;

public record PlatformsBalancesInsightsResponse(
    BalancesResponse balances,
    List<PlatformsInsights> platforms
) {
}
