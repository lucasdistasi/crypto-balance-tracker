package com.distasilucas.cryptobalancetracker.controller.swagger;

import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
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

import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_GOAL_UUID;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;

@Tag(name = "Goal Controller", description = "API endpoints for goal management")
public interface GoalControllerAPI {

    @Operation(summary = "Retrieve information for the given goal")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Goal information",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GoalResponse.class)
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
            description = "Goal not found",
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
    ResponseEntity<GoalResponse> retrieveGoalById(@UUID(message = INVALID_GOAL_UUID) String goalId);

    @Operation(summary = "Retrieve goals by page")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Goals by page",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = PageGoalResponse.class)
            )
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No goals found",
            content = @Content(
                mediaType = "application/json"
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
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProblemDetail.class))
            )
        )
    })
    ResponseEntity<PageGoalResponse> retrieveGoalsForPage(@Min(value = 0, message = INVALID_PAGE_NUMBER) int page);

    @Operation(summary = "Save goal")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Goal saved",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GoalResponse.class)
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
            responseCode = "500",
            description = "Internal Server Error",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ProblemDetail.class))
            )
        )
    })
    ResponseEntity<GoalResponse> saveGoal(@Valid GoalRequest goalRequest);

    @Operation(summary = "Update goal")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Goal updated",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GoalResponse.class)
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
            description = "Goal not found",
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
    ResponseEntity<GoalResponse> updateGoal(
        @UUID(message = INVALID_GOAL_UUID) String goalId,
        @Valid GoalRequest goalRequest
    );

    @Operation(summary = "Delete goal")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Goal deleted",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = GoalResponse.class)
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
            description = "Goal not found",
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
    ResponseEntity<Void> deleteGoal(@UUID(message = INVALID_GOAL_UUID) String goalId);
}
