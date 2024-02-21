package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
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
import java.math.RoundingMode;

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

        var goalEntity = goalRequest.toEntity(coingeckoCrypto.id());
        cryptoService.saveCryptoIfNotExists(coingeckoCrypto.id());
        var goal = goalRepository.save(goalEntity);

        log.info("Saved goal {}", goal);

        return mapToGoalResponse(goal);
    }

    public GoalResponse updateGoal(String goalId, GoalRequest goalRequest) {
        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));
        var updatedGoal = new Goal(goal.id(), goal.coingeckoCryptoId(), goalRequest.goalQuantity());

        goalRepository.save(updatedGoal);

        log.info("Updated goal. Before: {} | After: {}", goal, updatedGoal);

        return mapToGoalResponse(updatedGoal);
    }

    public void deleteGoal(String goalId) {
        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));

        goalRepository.deleteById(goalId);
        cryptoService.deleteCryptoIfNotUsed(goal.coingeckoCryptoId());

        log.info("Deleted goal {}", goal);
    }

    private GoalResponse mapToGoalResponse(Goal goal) {
        var crypto = cryptoService.retrieveCryptoInfoById(goal.coingeckoCryptoId());
        var actualQuantity = userCryptoService.findAllByCoingeckoCryptoId(goal.coingeckoCryptoId())
            .stream()
            .map(UserCrypto::quantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var progress = getProgress(goal.goalQuantity(), actualQuantity);
        var remainingQuantity = getRemainingQuantity(goal.goalQuantity(), actualQuantity);
        var moneyNeeded = getMoneyNeeded(remainingQuantity, crypto);

        return goal.toGoalResponse(goal.id(), crypto.name(), actualQuantity, progress, remainingQuantity, moneyNeeded);
    }

    private Float getProgress(BigDecimal goalQuantity, BigDecimal actualQuantity) {
        return goalQuantity.compareTo(actualQuantity) <= 0 ? 100F :
            actualQuantity.multiply(new BigDecimal("100"))
                .divide(goalQuantity, 2, RoundingMode.HALF_UP)
                .floatValue();
    }

    private BigDecimal getRemainingQuantity(BigDecimal goalQuantity, BigDecimal actualQuantity) {
        return goalQuantity.compareTo(actualQuantity) <= 0 ? BigDecimal.ZERO : goalQuantity.subtract(actualQuantity);
    }

    private BigDecimal getMoneyNeeded(BigDecimal remainingQuantity, Crypto crypto) {
        return crypto.lastKnownPrice().multiply(remainingQuantity).setScale(2, RoundingMode.HALF_UP);
    }
}
