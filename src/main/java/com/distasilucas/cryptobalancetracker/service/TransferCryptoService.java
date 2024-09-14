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
        var fromPlatform = platformService.retrievePlatformById(userCryptoToTransfer.getPlatform().getId());

        if (isToAndFromSamePlatform(toPlatform.getId(), fromPlatform.getId())) {
            throw new ApiValidationException(HttpStatus.BAD_REQUEST, SAME_FROM_TO_PLATFORM);
        }

        var availableQuantity = userCryptoToTransfer.getQuantity();
        var quantityToTransfer = transferCryptoRequest.quantityToTransfer();

        if (transferCryptoRequest.hasInsufficientBalance(availableQuantity)) {
            throw new InsufficientBalanceException();
        }

        var remainingCryptoQuantity = transferCryptoRequest.calculateRemainingCryptoQuantity(availableQuantity);
        var quantityToSendReceive = transferCryptoRequest.calculateQuantityToSendReceive(remainingCryptoQuantity, availableQuantity);
        var toPlatformOptionalUserCrypto = userCryptoService.findByCoingeckoCryptoIdAndPlatformId(
            userCryptoToTransfer.getCrypto().getId(),
            transferCryptoRequest.toPlatformId()
        );

        TransferCryptoResponse transferCryptoResponse = null;

        if (doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isPresent()) {
            var toPlatformUserCrypto = toPlatformOptionalUserCrypto.get();
            var newQuantity = toPlatformUserCrypto.getQuantity().add(quantityToSendReceive);
            var updatedFromPlatformUserCrypto = userCryptoToTransfer.withQuantity(remainingCryptoQuantity);
            var updatedToPlatformUserCrypto = toPlatformUserCrypto.withQuantity(newQuantity);

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
                quantityToSendReceive,
                toPlatform,
                userCryptoToTransfer.getCrypto()
            );
            var updatedUserCryptoToTransfer = userCryptoToTransfer.withQuantity(remainingCryptoQuantity);

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
            var newQuantity = toPlatformUserCrypto.getQuantity().add(quantityToSendReceive);

            var updatedToPlatformUserCrypto = toPlatformUserCrypto.withQuantity(newQuantity);

            userCryptoService.deleteUserCrypto(userCryptoToTransfer.getId());
            userCryptoService.saveOrUpdateAll(List.of(updatedToPlatformUserCrypto));

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                remainingCryptoQuantity,
                newQuantity,
                quantityToSendReceive
            );
        }

        if (!doesFromPlatformHaveRemaining(remainingCryptoQuantity) && toPlatformOptionalUserCrypto.isEmpty()) {
            var updatedFromPlatformUserCrypto = new UserCrypto(
                userCryptoToTransfer.getId(),
                quantityToSendReceive,
                toPlatform,
                userCryptoToTransfer.getCrypto()
            );

            if (updatedFromPlatformUserCrypto.getQuantity().compareTo(BigDecimal.ZERO) > 0) {
                userCryptoService.saveOrUpdateAll(List.of(updatedFromPlatformUserCrypto));
            } else {
                userCryptoService.deleteUserCrypto(updatedFromPlatformUserCrypto.getId());
            }

            transferCryptoResponse = transferCryptoRequest.toTransferCryptoResponse(
                remainingCryptoQuantity,
                quantityToSendReceive,
                quantityToSendReceive
            );
        }

        log.info("Transferred {} of {} from platform {} to {}", quantityToTransfer, userCryptoToTransfer.getCrypto().getId(), fromPlatform.getName(), toPlatform.getName());

        return transferCryptoResponse;
    }

    private boolean isToAndFromSamePlatform(String toPlatformId, String fromPlatformId) {
        return toPlatformId.equalsIgnoreCase(fromPlatformId);
    }

    private boolean doesFromPlatformHaveRemaining(BigDecimal remainingCryptoQuantity) {
        return remainingCryptoQuantity.compareTo(BigDecimal.ZERO) > 0;
    }
}
