package com.distasilucas.cryptobalancetracker.model.response.insights;

public record MarketData(
        String circulatingSupply,
        String maxSupply,
        CurrentPrice currentPrice,
        String marketCap,
        PriceChange priceChange
) {
}
