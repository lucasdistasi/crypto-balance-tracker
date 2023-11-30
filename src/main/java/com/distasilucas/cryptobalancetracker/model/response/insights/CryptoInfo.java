package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CryptoInfo(
        String id,
        String cryptoName,

        @JsonProperty("cryptoId")
        String coingeckoCryptoId,
        String symbol,
        String image
) {

    public CryptoInfo(String cryptoName, String coingeckoCryptoId, String symbol, String image) {
        this(null, cryptoName, coingeckoCryptoId, symbol, image);
    }
}
