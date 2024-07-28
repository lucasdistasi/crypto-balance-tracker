package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.controller.swagger.GoalControllerAPI;
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.service.GoalService;
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

import static com.distasilucas.cryptobalancetracker.constants.Constants.GOALS_ENDPOINT;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_GOAL_UUID;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;

@Validated
@RestController
@RequestMapping(GOALS_ENDPOINT)
@RequiredArgsConstructor
@CrossOrigin(origins = "${allowed-origins}")
public class GoalController implements GoalControllerAPI {

    private final GoalService goalService;
    private final UserCryptoService userCryptoService;

    @Override
    @GetMapping("/{goalId}")
    public ResponseEntity<GoalResponse> retrieveGoalById(@PathVariable @UUID(message = INVALID_GOAL_UUID) String goalId) {
        var goal = goalService.retrieveGoalById(goalId);
        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId(goal.getCrypto().getId());
        var goalResponse = goal.toGoalResponse(userCryptos);

        return ResponseEntity.ok(goalResponse);
    }

    @Override
    @GetMapping
    public ResponseEntity<PageGoalResponse> retrieveGoalsForPage(@RequestParam @Min(value = 0, message = INVALID_PAGE_NUMBER) int page) {
        var goals = goalService.retrieveGoalsForPage(page);
        var goalsResponse = goals.stream()
            .map(goal -> {
                var userCryptos = userCryptoService.findAllByCoingeckoCryptoId(goal.getCrypto().getId());
                return goal.toGoalResponse(userCryptos);
            })
            .toList();

        var pageGoalsResponse = new PageGoalResponse(page, goals.getTotalPages(), goalsResponse);

        return goals.isEmpty() ?
            ResponseEntity.noContent().build() :
            ResponseEntity.ok(pageGoalsResponse);
    }

    @Override
    @PostMapping
    public ResponseEntity<GoalResponse> saveGoal(@RequestBody @Valid GoalRequest goalRequest) {
        var goal = goalService.saveGoal(goalRequest);
        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId(goal.getCrypto().getId());
        var goalResponse = goal.toGoalResponse(userCryptos);

        return ResponseEntity.ok(goalResponse);
    }

    @Override
    @PutMapping("/{goalId}")
    public ResponseEntity<GoalResponse> updateGoal(
        @PathVariable @UUID(message = INVALID_GOAL_UUID) String goalId,
        @Valid @RequestBody GoalRequest goalRequest
    ) {
        var goal = goalService.updateGoal(goalId, goalRequest);
        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId(goal.getCrypto().getId());
        var goalResponse = goal.toGoalResponse(userCryptos);

        return ResponseEntity.ok(goalResponse);
    }

    @Override
    @DeleteMapping("/{goalId}")
    public ResponseEntity<Void> deleteGoal(@PathVariable @UUID(message = INVALID_GOAL_UUID) String goalId) {
        goalService.deleteGoal(goalId);

        return ResponseEntity.noContent().build();
    }
}
