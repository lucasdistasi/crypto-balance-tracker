package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.math.BigDecimal;

public record MarketData(
        String circulatingSupply,
        String maxSupply,
        CurrentPrice currentPrice,
        String marketCap,
        PriceChange priceChange
) {
}
