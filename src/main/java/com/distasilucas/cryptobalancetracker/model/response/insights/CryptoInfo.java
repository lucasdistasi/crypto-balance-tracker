package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
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

    public CryptoInfo(UserCrypto userCrypto, Crypto crypto) {
        this(
            userCrypto.getId(),
            crypto.getCryptoInfo().getName(),
            crypto.getId(),
            crypto.getCryptoInfo().getTicker(),
            crypto.getCryptoInfo().getImage()
        );
    }
}
