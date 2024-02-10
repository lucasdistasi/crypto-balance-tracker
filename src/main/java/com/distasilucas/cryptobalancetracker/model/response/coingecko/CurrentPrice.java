package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import java.io.Serializable;
import java.math.BigDecimal;

public record CurrentPrice(
    BigDecimal usd,
    BigDecimal eur,
    BigDecimal btc
) implements Serializable {
}
