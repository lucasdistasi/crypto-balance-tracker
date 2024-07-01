package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.distasilucas.cryptobalancetracker.entity.Crypto;

public record MarketData(
    CirculatingSupply circulatingSupply,
    String maxSupply,
    CurrentPrice currentPrice,
    String marketCap,
    PriceChange priceChange
) {

    public MarketData(CirculatingSupply circulatingSupply, Crypto crypto) {
        this(
            circulatingSupply,
            crypto.getMaxSupply().toPlainString(),
            new CurrentPrice(
                crypto.getLastKnownPrice().toPlainString(),
                crypto.getLastKnownPriceInEUR().toPlainString(),
                crypto.getLastKnownPriceInBTC().toPlainString()
            ),
            crypto.getMarketCap().toPlainString(),
            new PriceChange(
                crypto.getChangePercentageIn24h(),
                crypto.getChangePercentageIn7d(),
                crypto.getChangePercentageIn30d()
            )
        );
    }
}
