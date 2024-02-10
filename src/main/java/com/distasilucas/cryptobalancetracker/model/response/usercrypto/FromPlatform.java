package com.distasilucas.cryptobalancetracker.model.response.usercrypto;

public record FromPlatform(
    String userCryptoId,
    String networkFee,
    String quantityToTransfer,
    String totalToSubtract,
    String quantityToSendReceive,
    String remainingCryptoQuantity,
    boolean sendFullQuantity
) {
}
