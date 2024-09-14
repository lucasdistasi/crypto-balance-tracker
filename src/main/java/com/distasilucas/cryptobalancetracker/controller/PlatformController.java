package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.controller.swagger.PlatformControllerAPI;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse;
import com.distasilucas.cryptobalancetracker.service.PlatformService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_ENDPOINT;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(PLATFORMS_ENDPOINT)
@CrossOrigin(origins = "${allowed-origins}")
public class PlatformController implements PlatformControllerAPI {

    private final PlatformService platformService;

    @Override
    @GetMapping
    public ResponseEntity<List<PlatformResponse>> retrieveAllPlatforms() {
        var platforms = platformService.retrieveAllPlatforms()
            .stream()
            .map(Platform::toPlatformResponse)
            .toList();

        return platforms.isEmpty() ? ResponseEntity.noContent().build() : ResponseEntity.ok(platforms);
    }

    @Override
    @GetMapping("/{platformId}")
    public ResponseEntity<PlatformResponse> retrievePlatformById(
        @UUID(message = PLATFORM_ID_UUID)
        @PathVariable String platformId
    ) {
        var platform = platformService.retrievePlatformById(platformId);
        var platformResponse = platform.toPlatformResponse();

        return ResponseEntity.ok(platformResponse);
    }

    @Override
    @PostMapping
    public ResponseEntity<PlatformResponse> savePlatform(@Valid @RequestBody PlatformRequest platformRequest) {
        var platform = platformService.savePlatform(platformRequest);
        var platformResponse = platform.toPlatformResponse();

        return ResponseEntity.ok(platformResponse);
    }

    @Override
    @PutMapping("/{platformId}")
    public ResponseEntity<PlatformResponse> updatePlatform(
        @UUID(message = PLATFORM_ID_UUID) @PathVariable String platformId,
        @Valid @RequestBody PlatformRequest platformRequest
    ) {
        var platform = platformService.updatePlatform(platformId, platformRequest);
        var platformResponse = platform.toPlatformResponse();

        return ResponseEntity.ok(platformResponse);
    }

    @Override
    @DeleteMapping("/{platformId}")
    public ResponseEntity<Void> deletePlatform(@UUID(message = PLATFORM_ID_UUID) @PathVariable String platformId) {
        platformService.deletePlatform(platformId);

        return ResponseEntity.noContent().build();
    }
}
