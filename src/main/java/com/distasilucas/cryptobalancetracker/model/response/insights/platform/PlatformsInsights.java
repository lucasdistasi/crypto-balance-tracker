package com.distasilucas.cryptobalancetracker.model.response.insights.platform;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;

import java.io.Serializable;

public record PlatformsInsights(
    String platformName,
    BalancesResponse balances,
    float percentage
) implements Serializable {
}
