package com.distasilucas.cryptobalancetracker.model.response.usercrypto;

public record TransferCryptoResponse(
    FromPlatform fromPlatform,
    ToPlatform toPlatform
) {
}
