package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import java.math.BigDecimal;

public record MarketData(
        CurrentPrice currentPrice,
        BigDecimal circulatingSupply,
        BigDecimal maxSupply
) {
}
