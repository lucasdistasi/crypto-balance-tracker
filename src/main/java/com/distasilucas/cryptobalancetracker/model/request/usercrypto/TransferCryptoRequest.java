package com.distasilucas.cryptobalancetracker.model.request.usercrypto;

import com.distasilucas.cryptobalancetracker.model.response.usercrypto.FromPlatform;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.ToPlatform;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.TransferCryptoResponse;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.hibernate.validator.constraints.UUID;

import java.math.BigDecimal;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.NETWORK_FEE_DIGITS;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.NETWORK_FEE_MIN;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.NETWORK_FEE_NOT_NULL;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.QUANTITY_TO_TRANSFER_DECIMAL_MAX;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.QUANTITY_TO_TRANSFER_DIGITS;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.QUANTITY_TO_TRANSFER_NOT_NULL;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.QUANTITY_TO_TRANSFER_POSITIVE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.TO_PLATFORM_ID_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.TO_PLATFORM_ID_UUID;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.USER_CRYPTO_ID_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.USER_CRYPTO_ID_UUID;

public record TransferCryptoRequest(
    @NotBlank(message = USER_CRYPTO_ID_NOT_BLANK)
    @UUID(message = USER_CRYPTO_ID_UUID)
    String userCryptoId,

    @NotNull(message = QUANTITY_TO_TRANSFER_NOT_NULL)
    @Digits(integer = 16, fraction = 12, message = QUANTITY_TO_TRANSFER_DIGITS)
    @DecimalMax(value = "9999999999999999.999999999999", message = QUANTITY_TO_TRANSFER_DECIMAL_MAX)
    @Positive(message = QUANTITY_TO_TRANSFER_POSITIVE)
    BigDecimal quantityToTransfer,

    @NotNull(message = NETWORK_FEE_NOT_NULL)
    @Digits(integer = 16, fraction = 12, message = NETWORK_FEE_DIGITS)
    @Min(value = 0, message = NETWORK_FEE_MIN)
    BigDecimal networkFee,
    Boolean sendFullQuantity,

    @NotBlank(message = TO_PLATFORM_ID_NOT_BLANK)
    @UUID(message = TO_PLATFORM_ID_UUID)
    String toPlatformId
) {

    public TransferCryptoResponse toTransferCryptoResponse(
        BigDecimal remainingCryptoQuantity,
        BigDecimal newQuantity,
        BigDecimal quantityToSendReceive
    ) {
        var fromPlatform = new FromPlatform(
            userCryptoId,
            networkFee.toPlainString(),
            quantityToTransfer.toPlainString(),
            calculateTotalToSubtract(remainingCryptoQuantity).toPlainString(),
            quantityToSendReceive.toPlainString(),
            remainingCryptoQuantity.toPlainString(),
            sendFullQuantity
        );
        var toPlatform = new ToPlatform(toPlatformId, newQuantity.toPlainString());

        return new TransferCryptoResponse(fromPlatform, toPlatform);
    }

    public BigDecimal calculateTotalToSubtract(BigDecimal remainingCryptoQuantity) {
        if (Boolean.TRUE.equals(sendFullQuantity)) {
            return remainingCryptoQuantity.compareTo(BigDecimal.ZERO) > 0 ?
                networkFee.add(quantityToTransfer) : quantityToTransfer;
        } else {
            return quantityToTransfer;
        }
    }

    public BigDecimal calculateQuantityToSendReceive(BigDecimal remainingCryptoQuantity, BigDecimal availableQuantity) {
        BigDecimal quantityToSendReceive;

        if (Boolean.TRUE.equals(sendFullQuantity)) {
            quantityToSendReceive = remainingCryptoQuantity.equals(BigDecimal.ZERO) ? availableQuantity.subtract(networkFee) : quantityToTransfer;
        } else {
            quantityToSendReceive = quantityToTransfer.subtract(networkFee);
        }

        return quantityToSendReceive.stripTrailingZeros();
    }

    public BigDecimal calculateRemainingCryptoQuantity(BigDecimal availableQuantity) {
        BigDecimal remaining;
        if (Boolean.TRUE.equals(sendFullQuantity)) {
            remaining = availableQuantity.subtract(quantityToTransfer.add(networkFee));
        } else {
            remaining = availableQuantity.subtract(quantityToTransfer);
        }

        return remaining.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : remaining.stripTrailingZeros();
    }

    public boolean hasInsufficientBalance(BigDecimal availableQuantity) {
        return availableQuantity.compareTo(quantityToTransfer) < 0 || networkFee.compareTo(availableQuantity) > 0;
    }
}
