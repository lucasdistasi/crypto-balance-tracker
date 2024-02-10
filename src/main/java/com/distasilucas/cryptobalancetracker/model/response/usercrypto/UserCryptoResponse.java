package com.distasilucas.cryptobalancetracker.model.response.usercrypto;

public record UserCryptoResponse(
    String id,
    String cryptoName,
    String quantity,
    String platform
) {
}
