package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.controller.swagger.UserCryptoControllerAPI;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.TransferCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.TransferCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import com.distasilucas.cryptobalancetracker.service.TransferCryptoService;
import com.distasilucas.cryptobalancetracker.service.UserCryptoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.distasilucas.cryptobalancetracker.constants.Constants.USER_CRYPTOS_ENDPOINT;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.USER_CRYPTO_ID_UUID;

@Validated
@RestController
@RequestMapping(USER_CRYPTOS_ENDPOINT)
@RequiredArgsConstructor
@CrossOrigin(origins = "${allowed-origins}")
public class UserCryptoController implements UserCryptoControllerAPI {

    private final UserCryptoService userCryptoService;
    private final TransferCryptoService transferCryptoService;

    @Override
    @GetMapping("/{userCryptoId}")
    public ResponseEntity<UserCryptoResponse> retrieveUserCrypto(
        @UUID(message = USER_CRYPTO_ID_UUID)
        @PathVariable
        String userCryptoId
    ) {
        var userCrypto = userCryptoService.findUserCryptoById(userCryptoId);

        return ResponseEntity.ok(userCrypto.toUserCryptoResponse());
    }

    @Override
    @GetMapping
    public ResponseEntity<PageUserCryptoResponse> retrieveUserCryptosForPage(
        @Min(value = 0, message = INVALID_PAGE_NUMBER)
        @RequestParam
        int page
    ) {
        var userCryptos = userCryptoService.retrieveUserCryptosByPage(page);

        if (userCryptos.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            var userCryptosResponse = userCryptos.getContent()
                .stream()
                .map(UserCrypto::toUserCryptoResponse)
                .toList();
            var pageUserCryptos = new PageUserCryptoResponse(page, userCryptos.getTotalPages(), userCryptosResponse);

            return ResponseEntity.ok(pageUserCryptos);
        }
    }

    @Override
    @PostMapping
    public ResponseEntity<UserCryptoResponse> saveUserCrypto(@Valid @RequestBody UserCryptoRequest userCryptoRequest) {
        var userCrypto = userCryptoService.saveUserCrypto(userCryptoRequest);

        return ResponseEntity.ok(userCrypto.toUserCryptoResponse());
    }

    @Override
    @PutMapping("/{userCryptoId}")
    public ResponseEntity<UserCryptoResponse> updateUserCrypto(
        @UUID(message = USER_CRYPTO_ID_UUID)
        @PathVariable
        String userCryptoId,
        @Valid
        @RequestBody
        UserCryptoRequest userCryptoRequest
    ) {
        var userCrypto = userCryptoService.updateUserCrypto(userCryptoId, userCryptoRequest);

        return ResponseEntity.ok(userCrypto.toUserCryptoResponse());
    }

    @Override
    @DeleteMapping("/{userCryptoId}")
    public ResponseEntity<Void> deleteUserCrypto(
        @UUID(message = USER_CRYPTO_ID_UUID)
        @PathVariable
        String userCryptoId
    ) {
        userCryptoService.deleteUserCrypto(userCryptoId);

        return ResponseEntity.noContent().build();
    }

    @Override
    @PostMapping("/transfer")
    public ResponseEntity<TransferCryptoResponse> transferUserCrypto(
        @Valid
        @RequestBody
        TransferCryptoRequest transferCryptoRequest
    ) {
        var transferCryptoResponse = transferCryptoService.transferCrypto(transferCryptoRequest);

        return ResponseEntity.ok(transferCryptoResponse);
    }

}
