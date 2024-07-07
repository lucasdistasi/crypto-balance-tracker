package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedGoalException;
import com.distasilucas.cryptobalancetracker.exception.GoalNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_GOAL;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.GOAL_ID_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final CryptoService cryptoService;
    private final UserCryptoService userCryptoService;

    public GoalResponse retrieveGoalById(String goalId) {
        log.info("Retrieving goal for id {}", goalId);

        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));

        return mapToGoalResponse(goal);
    }

    public PageGoalResponse retrieveGoalsForPage(int page) {
        log.info("Retrieving pageGoals for page {}", page);

        var pageRequest = PageRequest.of(page, 10);
        var pageGoals = goalRepository.findAll(pageRequest);
        var goals = pageGoals.map(this::mapToGoalResponse).toList();

        return new PageGoalResponse(page, pageGoals.getTotalPages(), goals);
    }

    public GoalResponse saveGoal(GoalRequest goalRequest) {
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(goalRequest.cryptoName());
        var existingGoal = goalRepository.findByCoingeckoCryptoId(coingeckoCrypto.id());

        if (existingGoal.isPresent()) {
            throw new DuplicatedGoalException(DUPLICATED_GOAL.formatted(coingeckoCrypto.name()));
        }

        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id());
        var goalEntity = goalRequest.toEntity(crypto);
        var goal = goalRepository.save(goalEntity);

        log.info("Saved goal {}", goal);

        return mapToGoalResponse(goal);
    }

    public GoalResponse updateGoal(String goalId, GoalRequest goalRequest) {
        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));
        var updatedGoal = new Goal(goal.getId(), goalRequest.goalQuantity(), goal.getCrypto());

        goalRepository.save(updatedGoal);

        // FIXME - TODO - Best tostring()
        log.info("Updated goal. Before: {} | After: {}", goal, updatedGoal);

        return mapToGoalResponse(updatedGoal);
    }

    public void deleteGoal(String goalId) {
        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));

        goalRepository.deleteById(goalId);
        cryptoService.deleteCryptoIfNotUsed(goal.getCrypto().getId());

        log.info("Deleted goal {}", goal);
    }

    private GoalResponse mapToGoalResponse(Goal goal) {
        var coingeckoCryptoId = goal.getCrypto().getId();
        var actualQuantity = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId)
            .stream()
            .map(UserCrypto::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return goal.toGoalResponse(actualQuantity);
    }
}
