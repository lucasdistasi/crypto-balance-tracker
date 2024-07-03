package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "UserCryptos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCrypto implements Serializable {

    @Id
    private String id;

    private BigDecimal quantity;

    @ManyToOne
    @JoinColumn(name = "platform_id")
    private Platform platform;

    @ManyToOne
    @JoinColumn(name = "crypto_id")
    private Crypto crypto;

    public UserCrypto(BigDecimal quantity, Platform platform, Crypto crypto) {
        this.id = UUID.randomUUID().toString();
        this.quantity = quantity;
        this.platform = platform;
        this.crypto = crypto;
    }

    public UserCryptoResponse toUserCryptoResponse(String cryptoName, String platformName) {
        return new UserCryptoResponse(id, cryptoName, quantity.toPlainString(), platformName);
    }

    public UserCrypto withQuantity(BigDecimal updatedQuantity) {
        return new UserCrypto(id, updatedQuantity, platform, crypto);
    }

    public UserCrypto toUpdatedUserCrypto(BigDecimal quantity,  Platform platform) {
        return new UserCrypto(this.id, quantity, platform, this.crypto);
    }
}
