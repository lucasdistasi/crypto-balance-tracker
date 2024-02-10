package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import java.io.Serializable;
import java.math.BigDecimal;

public record MarketCap(
    BigDecimal usd
) implements Serializable {
}
