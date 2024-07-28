package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public record DatesBalanceResponse(
    List<DateBalances> datesBalances,
    BalanceChanges change,
    DifferencesChanges priceDifference
) implements Serializable {

    public static DatesBalanceResponse empty() {
        var emptyChange = new BalanceChanges(0F, 0F, 0F);
        var emptyDifferencesChanges = new DifferencesChanges("0", "0", "0");

        return new DatesBalanceResponse(Collections.emptyList(), emptyChange, emptyDifferencesChanges);
    }
}
