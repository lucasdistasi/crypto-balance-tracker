package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.math.BigDecimal;

public record PriceChange(
    BigDecimal changePercentageIn24h,
    BigDecimal changePercentageIn7d,
    BigDecimal changePercentageIn30d
) {
}
