package com.distasilucas.cryptobalancetracker.controller.swagger;

import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.SortBy;
import com.distasilucas.cryptobalancetracker.model.SortType;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import org.hibernate.validator.constraints.UUID;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;

@Tag(name = "Insights Controller", description = "API endpoints for retrieving insights")
public interface InsightsControllerAPI {

    @Operation(summary = "Retrieve total balances in USD, BTC and EUR")
    @ApiResponse(
        responseCode = "200",
        description = "Total Balances",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = BalancesResponse.class)
        )
    )
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
    ResponseEntity<BalancesResponse> retrieveTotalBalancesInsights();

    @Operation(summary = "Retrieve balances for the given Date Range")
    @ApiResponse(
        responseCode = "200",
        description = "Balances",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = DatesBalanceResponse.class)
        )
    )
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
    ResponseEntity<DatesBalanceResponse> retrieveDatesBalancesResponse(DateRange dateRange);

    @Operation(summary = "Retrieves information of each user crypto, like its balance, information about the crypto, where it's stored")
    @ApiResponse(
        responseCode = "200",
        description = "Cryptos Information",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PageUserCryptosInsightsResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved"
    )
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
    ResponseEntity<PageUserCryptosInsightsResponse> retrieveUserCryptosInsights(
        @Min(value = 0, message = "Page must be greater than or equal to 0")
        int page,
        SortBy sortBy,
        SortType sortType
    );

    @Operation(summary = "Retrieves information of each INDIVIDUAL user crypto, like the total balance, information about the crypto, in which platforms it's stored")
    @ApiResponse(
        responseCode = "200",
        description = "Cryptos Information",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PageUserCryptosInsightsResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved"
    )
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
    ResponseEntity<PageUserCryptosInsightsResponse> retrieveUserCryptosPlatformsInsights(
        @Min(value = 0, message = "Page must be greater than or equal to 0")
        int page,
        SortBy sortBy,
        SortType sortType
    );

    @Operation(summary = "Retrieve insights balances for all user cryptos")
    @ApiResponse(
        responseCode = "200",
        description = "User cryptos balances insights",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CryptosBalancesInsightsResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved"
    )
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
    ResponseEntity<CryptosBalancesInsightsResponse> retrieveCryptosBalancesInsights();

    @Operation(summary = "Retrieve insights balances for all platforms")
    @ApiResponse(
        responseCode = "200",
        description = "Platforms balances insights",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PlatformsBalancesInsightsResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved"
    )
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
    ResponseEntity<PlatformsBalancesInsightsResponse> retrievePlatformsBalancesInsights();

    @Operation(summary = "Retrieve user cryptos insights for the given coingecko crypto id")
    @ApiResponse(
        responseCode = "200",
        description = "User cryptos insights",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = CryptoInsightResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved"
    )
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
    ResponseEntity<CryptoInsightResponse> retrieveCryptoInsights(String coingeckoCryptoId);

    @Operation(summary = "Retrieve insights for the given platform")
    @ApiResponse(
        responseCode = "200",
        description = "Platform insights",
        content = @Content(
            mediaType = "application/json",
            schema = @Schema(implementation = PlatformInsightsResponse.class)
        )
    )
    @ApiResponse(
        responseCode = "204",
        description = "No user cryptos saved"
    )
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
    ResponseEntity<PlatformInsightsResponse> retrievePlatformInsights(@UUID(message = PLATFORM_ID_UUID) String platformId);
}
