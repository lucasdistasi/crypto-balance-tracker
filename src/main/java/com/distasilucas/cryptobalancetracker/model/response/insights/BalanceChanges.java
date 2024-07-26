package com.distasilucas.cryptobalancetracker.model.response.insights;

import java.io.Serializable;

public record BalanceChanges(
    float usdChange,
    float eurChange,
    float btcChange
) implements Serializable {
}
