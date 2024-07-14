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
            crypto.getCryptoInfo().getMaxSupply().toPlainString(),
            new CurrentPrice(
                crypto.getLastKnownPrices().getLastKnownPrice().toPlainString(),
                crypto.getLastKnownPrices().getLastKnownPriceInEUR().toPlainString(),
                crypto.getLastKnownPrices().getLastKnownPriceInBTC().toPlainString()
            ),
            crypto.getCryptoInfo().getMarketCap().toPlainString(),
            new PriceChange(
                crypto.getChangePercentages().getChangePercentageIn24h(),
                crypto.getChangePercentages().getChangePercentageIn7d(),
                crypto.getChangePercentages().getChangePercentageIn30d()
            )
        );
    }
}
