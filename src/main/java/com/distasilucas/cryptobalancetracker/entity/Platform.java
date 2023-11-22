package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("Platforms")
public record Platform(
        String id,
        String name
) {

    public PlatformResponse toPlatformResponse() {
        return new PlatformResponse(id, name);
    }
}
