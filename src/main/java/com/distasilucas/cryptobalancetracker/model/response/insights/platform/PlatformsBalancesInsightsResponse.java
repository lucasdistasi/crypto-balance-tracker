package com.distasilucas.cryptobalancetracker.model.response.insights.platform;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record PlatformsBalancesInsightsResponse(
    BalancesResponse balances,
    List<PlatformsInsights> platforms
) implements Serializable {

    public static PlatformsBalancesInsightsResponse empty() {
        return new PlatformsBalancesInsightsResponse(BalancesResponse.empty(), Collections.emptyList());
    }
}
