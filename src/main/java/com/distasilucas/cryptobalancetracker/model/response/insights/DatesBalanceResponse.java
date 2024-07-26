package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.io.Serializable;
import java.util.List;

public record DatesBalanceResponse(
    List<DateBalances> datesBalances,
    BalanceChanges change,
    DifferencesChanges priceDifference
) implements Serializable {
}
