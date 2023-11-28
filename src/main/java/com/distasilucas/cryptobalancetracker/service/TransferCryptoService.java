package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.ApiValidationException;
import com.distasilucas.cryptobalancetracker.exception.InsufficientBalanceException;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.TransferCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.TransferCryptoResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.SAME_FROM_TO_PLATFORM;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferCryptoService {

    private final UserCryptoService userCryptoService;
    private final PlatformService platformService;

    public TransferCryptoResponse transferCrypto(TransferCryptoRequest transferCryptoRequest) {
        var toPlatform = platformService.retrievePlatformById(transferCryptoRequest.toPlatformId());
        var userCryptoToTransfer = userCryptoService.findUserCryptoById(transferCryptoRequest.userCryptoId());
        var fromPlatform = platformService.retrievePlatformById(userCryptoToTransfer.platformId());

        if (isToAndFromSamePlatform(toPlatform.id(), fromPlatform.id())) {
            throw new ApiValidationException(HttpStatus.BAD_REQUEST, SAME_FROM_TO_PLATFORM);
        }

        var availableQuantity = userCryptoToTransfer.quantity();
        var quantityToTransfer = transferCryptoRequest.quantityToTransfer();

        if (transferCryptoRequest.hasInsufficientBalance(availableQuantity)) {
            throw new InsufficientBalanceException();
        }

        var remainingCryptoQuantity = transferCryptoRequest.calculateRemainingCryptoQuantity(availableQuantity);
        var quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity, availableQuantity);
        var toPlatformOptionalUserCrypto = userCryptoService.findByCoingeckoCryptoIdAndPlatformId(
                userCryptoToTransfer.coingeckoCryptoId(),
                transferCryptoRequest.toPlatformId()
        );

        TransferCryptoResponse transferCryptoResponse = null;

        if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isPresent()) {
            var toPlatformUserCrypto = toPlatformOptionalUserCrypto.get();
            var newQuantity = toPlatformUserCrypto.quantity().add(quantityToSendReceive);
            var updatedFromPlatformUserCrypto = userCryptoToTransfer.copy(remainingCryptoQuantity);
            var updatedToPlatformUserCrypto = toPlatformUserCrypto.copy(newQuantity);

            userCryptoService.saveOrUpdateAll(List.of(updatedFromPlatformUserCrypto, updatedToPlatformUserCrypto));

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                    remainingCryptoQuantity,
                    newQuantity,
                    quantityToSendReceive
            );
        }

        if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isEmpty()) {
            var uuid = UUID.randomUUID().toString();
            var toPlatformUserCrypto = new UserCrypto(
                    uuid,
                    userCryptoToTransfer.coingeckoCryptoId(),
                    quantityToSendReceive,
                    transferCryptoRequest.toPlatformId()
            );
            var updatedUserCryptoToTransfer = userCryptoToTransfer.copy(remainingCryptoQuantity);

            if (Boolean.TRUE.equals(transferCryptoRequest.sendFullQuantity())) {
                userCryptoService.saveOrUpdateAll(List.of(updatedUserCryptoToTransfer, toPlatformUserCrypto));
            } else {
                if (quantityToSendReceive.compareTo(BigDecimal.ZERO) > 0) {
                    userCryptoService.saveOrUpdateAll(List.of(updatedUserCryptoToTransfer, toPlatformUserCrypto));
                } else {
                    userCryptoService.saveOrUpdateAll(List.of(updatedUserCryptoToTransfer));
                }
            }

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                    remainingCryptoQuantity,
                    quantityToSendReceive,
                    quantityToSendReceive
            );
        }

        if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isPresent()) {
            var toPlatformUserCrypto = toPlatformOptionalUserCrypto.get();
            var newQuantity = toPlatformUserCrypto.quantity().add(quantityToSendReceive);

            var updatedToPlatformUserCrypto = toPlatformUserCrypto.copy(newQuantity);

            userCryptoService.deleteUserCrypto(userCryptoToTransfer.id());
            userCryptoService.saveOrUpdateAll(List.of(updatedToPlatformUserCrypto));

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                    remainingCryptoQuantity,
                    newQuantity,
                    quantityToSendReceive
            );
        }

        if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isEmpty()) {
            var updatedFromPlatformUserCrypto = new UserCrypto(
                    userCryptoToTransfer.id(),
                    userCryptoToTransfer.coingeckoCryptoId(),
                    quantityToSendReceive,
                    toPlatform.id()
            );

            if (updatedFromPlatformUserCrypto.quantity().compareTo(BigDecimal.ZERO) > 0) {
                userCryptoService.saveOrUpdateAll(List.of(updatedFromPlatformUserCrypto));
            } else {
                userCryptoService.deleteUserCrypto(updatedFromPlatformUserCrypto.id());
            }

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                    remainingCryptoQuantity,
                    quantityToSendReceive,
                    quantityToSendReceive
            );
        }

        log.info("Transferred {} of {} from platform {} to {}", quantityToTransfer, userCryptoToTransfer.coingeckoCryptoId(), fromPlatform.name(), toPlatform.name());

        return transferCryptoResponse;
    }

    private boolean isToAndFromSamePlatform(String toPlatformId, String fromPlatformId) {
        return toPlatformId.equalsIgnoreCase(fromPlatformId);
    }

    private boolean doesFromPlatformHaveRemaining(BigDecimal remainingCryptoQuantity) {
        return remainingCryptoQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
