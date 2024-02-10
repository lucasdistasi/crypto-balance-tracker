package com.distasilucas.cryptobalancetracker.model.request.platform;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.validation.ValidPlatformName;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.NULL_BLANK_PLATFORM_NAME;

public record PlatformRequest(
    @NotBlank(message = NULL_BLANK_PLATFORM_NAME)
    @ValidPlatformName
    String name
) {

    public Platform toEntity() {
        return new Platform(UUID.randomUUID().toString(), name.toUpperCase());
    }
}
