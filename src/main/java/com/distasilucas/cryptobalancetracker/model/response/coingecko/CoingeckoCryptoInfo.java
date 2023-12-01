package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import java.io.Serializable;

public record CoingeckoCryptoInfo(
        String id,
        String symbol,
        String name,
        Image image,
        MarketData marketData
) implements Serializable {
}
