package com.distasilucas.cryptobalancetracker.model.response.insights;

public record MarketData(
    CirculatingSupply circulatingSupply,
    String maxSupply,
    CurrentPrice currentPrice,
    String marketCap,
    PriceChange priceChange
) {
}
