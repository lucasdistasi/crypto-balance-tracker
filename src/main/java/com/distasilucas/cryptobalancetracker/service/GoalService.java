package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedGoalException;
import com.distasilucas.cryptobalancetracker.exception.GoalNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import static com.distasilucas.cryptobalancetracker.constants.Constants.GOAL_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PAGE_GOALS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_GOAL;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.GOAL_ID_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final CryptoService cryptoService;
    private final CacheService cacheService;

    @Cacheable(cacheNames = GOAL_CACHE, key = "#goalId")
    public Goal retrieveGoalById(String goalId) {
        log.info("Retrieving goal for id {}", goalId);

        return goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));
    }

    @Cacheable(cacheNames = PAGE_GOALS_CACHE, key = "#page")
    public Page<Goal> retrieveGoalsForPage(int page) {
        log.info("Retrieving pageGoals for page {}", page);
        var pageRequest = PageRequest.of(page, 10);

        return goalRepository.findAll(pageRequest);
    }

    public Goal saveGoal(GoalRequest goalRequest) {
        var coingeckoCrypto = cryptoService.retrieveCoingeckoCryptoInfoByNameOrId(goalRequest.cryptoName());
        var existingGoal = goalRepository.findByCoingeckoCryptoId(coingeckoCrypto.id());

        if (existingGoal.isPresent()) {
            throw new DuplicatedGoalException(DUPLICATED_GOAL.formatted(coingeckoCrypto.name()));
        }

        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCrypto.id());
        var goalEntity = goalRequest.toEntity(crypto);
        var goal = goalRepository.save(goalEntity);
        cacheService.invalidateGoalsCaches();

        log.info("Saved goal {}", goal);

        return goal;
    }

    public Goal updateGoal(String goalId, GoalRequest goalRequest) {
        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));
        var updatedGoal = goal.withNewGoalQuantity(goalRequest.goalQuantity());

        log.info("Updating goal. Before: {} | After: {}", goal, updatedGoal);
        var goalUpdated = goalRepository.save(updatedGoal);
        cacheService.invalidateGoalsCaches();

        return goalUpdated;
    }

    public void deleteGoal(String goalId) {
        var goal = goalRepository.findById(goalId)
            .orElseThrow(() -> new GoalNotFoundException(GOAL_ID_NOT_FOUND.formatted(goalId)));

        goalRepository.deleteById(goalId);
        cryptoService.deleteCryptoIfNotUsed(goal.getCrypto().getId());
        cacheService.invalidateGoalsCaches();

        log.info("Deleted goal {}", goal);
    }
}
