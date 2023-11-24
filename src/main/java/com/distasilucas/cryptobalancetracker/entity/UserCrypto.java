package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.util.UUID;

@Document("UserCryptos")
public record UserCrypto(
        @Id
        String id,

        @Field("crypto_id")
        String coingeckoCryptoId,
        BigDecimal quantity,

        @Field("platform_id")
        String platformId
) {

    public UserCrypto(String coingeckoCryptoId, BigDecimal quantity, String platformId) {
        this(UUID.randomUUID().toString(), coingeckoCryptoId, quantity, platformId);
    }

    public UserCryptoResponse toUserCryptoResponse(String cryptoName, String platformName) {
        return new UserCryptoResponse(id, cryptoName, quantity.toPlainString(), platformName);
    }
}
