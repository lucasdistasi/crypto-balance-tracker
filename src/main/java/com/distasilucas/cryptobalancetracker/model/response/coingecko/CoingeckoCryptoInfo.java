package com.distasilucas.cryptobalancetracker.model.response.coingecko;

public record CoingeckoCryptoInfo(
        String id,
        String symbol,
        String name,
        Image image,
        MarketData marketData
) {
}
