package com.distasilucas.cryptobalancetracker.model.request.pricetarget;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import com.distasilucas.cryptobalancetracker.validation.ValidCryptoName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_SIZE;

public record PriceTargetRequest(
    @NotBlank(message = CRYPTO_NAME_NOT_BLANK)
    @Size(min = 1, max = 64, message = CRYPTO_NAME_SIZE)
    @ValidCryptoName
    String cryptoNameOrId,

    @NotNull(message = "Price target can not be null")
    @Digits(
        integer = 16,
        fraction = 12,
        message = "Price target must have up to {integer} digits in the integer part and up to {fraction} digits in the decimal part"
    )
    @DecimalMax(
        value = "9999999999999999.999999999999",
        message = "Price target must be less than or equal to 9999999999999999.999999999999"
    )
    @Positive(message = "Price target must be greater than 0")
    BigDecimal priceTarget
) {

    public PriceTarget toEntity(Crypto crypto) {
        return new PriceTarget(priceTarget, crypto);
    }
}
