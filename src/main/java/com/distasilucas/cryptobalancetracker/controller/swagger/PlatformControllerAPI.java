package com.distasilucas.cryptobalancetracker.controller.swagger;

import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.model.response.platform.PlatformResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;

@Tag(name = "Platform Controller", description = "API endpoints for platform management")
public interface PlatformControllerAPI {

    @Operation(summary = "Retrieve all platforms")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Platforms",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = PlatformResponse.class
                ))
            )),
        @ApiResponse(
            responseCode = "204",
            description = "No platforms saved",
            content = @Content(
                mediaType = "application/json"
            )),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            ))}
    )
    ResponseEntity<List<PlatformResponse>> retrieveAllPlatforms();

    @Operation(summary = "Retrieve platform")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Platform information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlatformResponse.class)
            )),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "404",
            description = "Platform not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            ))}
    )
    ResponseEntity<PlatformResponse> retrievePlatformById(@UUID(message = PLATFORM_ID_UUID) String platformId);

    @Operation(summary = "Save platform")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Platform saved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlatformResponse.class)
            )),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            ))}
    )
    ResponseEntity<PlatformResponse> savePlatform(@Valid PlatformRequest platformRequest);

    @Operation(summary = "Update platform")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Platform updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PlatformResponse.class)
            )),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "404",
            description = "Platform not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            ))}
    )
    ResponseEntity<PlatformResponse> updatePlatform(
        @UUID(message = PLATFORM_ID_UUID) String platformId,
        @Valid PlatformRequest platformRequest
    );

    @Operation(summary = "Delete platform")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Platform deleted",
            content = @Content(
                mediaType = "application/json"
            )),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "404",
            description = "Platform not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            )),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(
                    implementation = ProblemDetail.class
                ))
            ))}
    )
    ResponseEntity<Void> deletePlatform(@UUID(message = PLATFORM_ID_UUID) String platformId);
}
