package com.distasilucas.cryptobalancetracker.model;

import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights;

import java.math.BigDecimal;
import java.util.Comparator;

public enum SortBy {
    PERCENTAGE(Comparator.comparing(UserCryptosInsights::percentage)),
    MARKET_CAP_RANK(Comparator.comparing(UserCryptosInsights::marketCapRank)),
    CURRENT_PRICE(Comparator.comparing(crypto -> new BigDecimal(crypto.marketData().currentPrice().usd()))),
    MAX_SUPPLY(Comparator.comparing(crypto -> new BigDecimal(crypto.marketData().maxSupply()))),
    CHANGE_PRICE_IN_24H(Comparator.comparing(crypto -> crypto.marketData().priceChange().changePercentageIn24h())),
    CHANGE_PRICE_IN_7D(Comparator.comparing(crypto -> crypto.marketData().priceChange().changePercentageIn7d())),
    CHANGE_PRICE_IN_30D(Comparator.comparing(crypto -> crypto.marketData().priceChange().changePercentageIn30d()));

    private final Comparator<UserCryptosInsights> userCryptosInsightsComparator;

    SortBy(Comparator<UserCryptosInsights> comparator) {
        this.userCryptosInsightsComparator = comparator;
    }

    public Comparator<UserCryptosInsights> getUserCryptosInsightsComparator(SortType sortType) {
        return sortType == SortType.ASC ? userCryptosInsightsComparator : userCryptosInsightsComparator.reversed();
    }
}
