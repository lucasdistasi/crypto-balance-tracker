package com.distasilucas.cryptobalancetracker.controller.swagger;

import com.distasilucas.cryptobalancetracker.model.request.usercrypto.TransferCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.TransferCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.USER_CRYPTO_ID_UUID;

@Tag(name = "UserCrypto Controller", description = "API endpoints for user cryptos management")
public interface UserCryptoControllerAPI {

    @Operation(summary = "Retrieve information for the given user crypto id")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User crypto information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCryptoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                ))
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User crypto not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                ))
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                ))
        )
    })
    ResponseEntity<UserCryptoResponse> retrieveUserCrypto(@UUID(message = USER_CRYPTO_ID_UUID) String userCryptoId);


    @Operation(summary = "Retrieves user cryptos by page")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User cryptos by page",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCryptoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No user cryptos found",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        )
    })
    ResponseEntity<PageUserCryptoResponse> retrieveUserCryptosForPage(@Min(value = 0, message = INVALID_PAGE_NUMBER) int page);


    @Operation(summary = "Save user crypto")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User crypto saved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCryptoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class))
            )
        )
    })
    ResponseEntity<UserCryptoResponse> saveUserCrypto(@Valid UserCryptoRequest userCryptoRequest);


    @Operation(summary = "Update user crypto")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User crypto updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCryptoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User crypto not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        )
    })
    ResponseEntity<UserCryptoResponse> updateUserCrypto(
        @UUID(message = USER_CRYPTO_ID_UUID) String userCryptoId,
        @Valid UserCryptoRequest userCryptoRequest
    );


    @Operation(summary = "Delete user crypto")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "User crypto deleted",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UserCryptoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User crypto or platform not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(
                    schema = @Schema(implementation = ProblemDetail.class)
                )
            )
        )
    })
    ResponseEntity<Void> deleteUserCrypto(@UUID(message = USER_CRYPTO_ID_UUID) String userCryptoId);

    @Operation(summary = "Transfer user crypto")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "User crypto transferred",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = TransferCryptoResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Bad request",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProblemDetail.class))
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "User crypto not found, Platform not found",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProblemDetail.class))
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProblemDetail.class))
            )
        )
    })
    ResponseEntity<TransferCryptoResponse> transferUserCrypto(@Valid TransferCryptoRequest transferCryptoRequest);

}
