package com.distasilucas.cryptobalancetracker.model.response.usercrypto;

public record ToPlatform(
    String platformId,
    String newQuantity
) {
}
