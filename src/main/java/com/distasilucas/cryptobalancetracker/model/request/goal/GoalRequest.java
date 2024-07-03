package com.distasilucas.cryptobalancetracker.model.request.goal;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.validation.ValidCryptoName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_SIZE;

public record GoalRequest(

    @NotBlank(message = CRYPTO_NAME_NOT_BLANK)
    @Size(min = 1, max = 64, message = CRYPTO_NAME_SIZE)
    @ValidCryptoName
    String cryptoName,

    @NotNull(message = "Goal quantity can not be null")
    @Digits(
        integer = 16,
        fraction = 12,
        message = "Goal quantity must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
    )
    @DecimalMax(
        value = "9999999999999999.999999999999",
        message = "Goal quantity must be less than or equal to 9999999999999999.999999999999"
    )
    @Positive(message = "Goal quantity must be greater than 0")
    BigDecimal goalQuantity
) {

    public Goal toEntity(Crypto crypto) {
        return new Goal(UUID.randomUUID().toString(), goalQuantity, crypto);
    }
}
