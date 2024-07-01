package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "UserCryptos")
public record UserCrypto(
    @Id
    String id,

    @Column(name = "crypto_id")
    String coingeckoCryptoId,

    BigDecimal quantity,

    @ManyToOne
    @JoinColumn(name = "platform_id")
    Platform platform
) implements Serializable {

    public UserCrypto(String coingeckoCryptoId, BigDecimal quantity, Platform platform) {
        this(UUID.randomUUID().toString(), coingeckoCryptoId, quantity, platform);
    }

    public UserCryptoResponse toUserCryptoResponse(String cryptoName, String platformName) {
        return new UserCryptoResponse(id, cryptoName, quantity.toPlainString(), platformName);
    }

    public UserCrypto withQuantity(BigDecimal updatedQuantity) {
        return new UserCrypto(id, coingeckoCryptoId, updatedQuantity, platform);
    }
}
