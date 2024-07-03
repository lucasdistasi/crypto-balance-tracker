package com.distasilucas.cryptobalancetracker.model.request.usercrypto;

import com.distasilucas.cryptobalancetracker.validation.ValidCryptoName;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_SIZE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_DECIMAL_MAX;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_DIGITS;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_NOT_NULL;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_POSITIVE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;

public record UserCryptoRequest(
    @NotBlank(message = CRYPTO_NAME_NOT_BLANK)
    @Size(min = 1, max = 64, message = CRYPTO_NAME_SIZE)
    @ValidCryptoName
    String cryptoName,

    @NotNull(message = CRYPTO_QUANTITY_NOT_NULL)
    @Digits(integer = 16, fraction = 12, message = CRYPTO_QUANTITY_DIGITS)
    @DecimalMax(value = "9999999999999999.999999999999", message = CRYPTO_QUANTITY_DECIMAL_MAX)
    @Positive(message = CRYPTO_QUANTITY_POSITIVE)
    BigDecimal quantity,

    @NotBlank(message = PLATFORM_ID_NOT_BLANK)
    @UUID(message = PLATFORM_ID_UUID)
    String platformId
) {
}
