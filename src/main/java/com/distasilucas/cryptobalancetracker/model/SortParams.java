package com.distasilucas.cryptobalancetracker.model;

import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights;

import java.util.Comparator;

public record SortParams(
    SortBy sortBy,
    SortType sortType
) {

    public Comparator<UserCryptosInsights> cryptosInsightsResponseComparator() {
        return sortBy.getUserCryptosInsightsComparator(sortType);
    }
}
