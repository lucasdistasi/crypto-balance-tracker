package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.util.List;

public record DatesBalanceResponse(
    List<DatesBalances> datesBalances,
    float change,
    String priceDifference
) {
}
