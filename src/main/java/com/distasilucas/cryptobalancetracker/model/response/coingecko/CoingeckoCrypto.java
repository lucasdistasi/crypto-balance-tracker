package com.distasilucas.cryptobalancetracker.model.response.coingecko;

import java.io.Serializable;

public record CoingeckoCrypto(
    String id,
    String symbol,
    String name
) implements Serializable {
}
