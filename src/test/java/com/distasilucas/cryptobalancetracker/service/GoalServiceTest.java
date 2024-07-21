package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedGoalException;
import com.distasilucas.cryptobalancetracker.exception.GoalNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalRequest;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_GOAL;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.GOAL_ID_NOT_FOUND;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class GoalServiceTest {

    @Mock
    private GoalRepository goalRepositoryMock;

    @Mock
    private CryptoService cryptoServiceMock;

    @Mock
    private CacheService cacheServiceMock;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        goalService = new GoalService(goalRepositoryMock, cryptoServiceMock, cacheServiceMock);
    }

    @Test
    void shouldRetrieveGoalById() {
        var goalEntity = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", new BigDecimal("1"), getBitcoinCryptoEntity());
        var expected = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", new BigDecimal("1"), getBitcoinCryptoEntity());

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goalEntity));

        var goal = goalService.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(goal)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void shouldThrowGoalNotFoundExceptionWhenRetrievingGoalById() {
        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.empty());

        var exception = assertThrows(
            GoalNotFoundException.class,
            () -> goalService.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3")
        );

        assertEquals(GOAL_ID_NOT_FOUND.formatted("10e3c7c1-0732-4294-9410-9708a21128e3"), exception.getMessage());
    }

    @Test
    void shouldRetrieveGoalsForPage() {
        var pageRequest = PageRequest.of(0, 10);
        var goal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", new BigDecimal("1"), getBitcoinCryptoEntity());

        when(goalRepositoryMock.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(goal), pageRequest, 10));

        var pageGoals = goalService.retrieveGoalsForPage(0);

        assertThat(pageGoals)
            .usingRecursiveComparison()
            .isEqualTo(new PageImpl<>(List.of(goal), pageRequest, 10));
    }

    @Test
    void shouldSaveGoal() {
        var goalRequest = getGoalRequest();
        var coingeckoCrypto = getCoingeckoCrypto();

        var captor = ArgumentCaptor.forClass(Goal.class);

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(coingeckoCrypto);
        when(goalRepositoryMock.findByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.empty());
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(getBitcoinCryptoEntity());
        when(goalRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var goalResponse = goalService.saveGoal(goalRequest);

        verify(goalRepositoryMock, times(1)).save(captor.getValue());
        verify(cacheServiceMock, times(1)).invalidateGoalsCaches();
        assertThat(goalResponse)
            .usingRecursiveComparison()
            .isEqualTo(new Goal(captor.getValue().getId(), new BigDecimal("1"), getBitcoinCryptoEntity()));
    }

    @Test
    void shouldThrowDuplicatedGoalExceptionWhenSavingGoal() {
        var goalRequest = getGoalRequest();
        var existingGoal = getGoalEntity();

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByNameOrId("bitcoin")).thenReturn(getCoingeckoCrypto());
        when(goalRepositoryMock.findByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.of(existingGoal));

        var exception = assertThrows(
            DuplicatedGoalException.class,
            () -> goalService.saveGoal(goalRequest)
        );

        verify(goalRepositoryMock, never()).save(any());
        assertEquals(DUPLICATED_GOAL.formatted("Bitcoin"), exception.getMessage());
    }

    @Test
    void shouldUpdateGoal() {
        var goalRequest = new GoalRequest("bitcoin", new BigDecimal("0.75"));
        var goal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", new BigDecimal("1"), getBitcoinCryptoEntity());
        var updatedGoal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", new BigDecimal("0.75"), getBitcoinCryptoEntity());
        var captor = ArgumentCaptor.forClass(Goal.class);

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goal));
        when(goalRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());

        var goalUpdated = goalService.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest);

        verify(cacheServiceMock, times(1)).invalidateGoalsCaches();
        assertThat(goalUpdated)
            .usingRecursiveComparison()
            .isEqualTo(updatedGoal);
    }

    @Test
    void shouldThrowGoalNotFoundExceptionWhenUpdatingGoal() {
        var goalRequest = getGoalRequest();

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.empty());

        var exception = assertThrows(
            GoalNotFoundException.class,
            () -> goalService.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest)
        );

        assertEquals(GOAL_ID_NOT_FOUND.formatted("10e3c7c1-0732-4294-9410-9708a21128e3"), exception.getMessage());
    }

    @Test
    void shouldDeleteGoal() {
        var goalEntity = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", new BigDecimal("1"), getBitcoinCryptoEntity());

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goalEntity));
        doNothing().when(goalRepositoryMock).deleteById("10e3c7c1-0732-4294-9410-9708a21128e3");
        doNothing().when(cryptoServiceMock).deleteCryptoIfNotUsed("bitcoin");

        goalService.deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        verify(goalRepositoryMock, times(1)).deleteById("10e3c7c1-0732-4294-9410-9708a21128e3");
        verify(cryptoServiceMock, times(1)).deleteCryptoIfNotUsed("bitcoin");
        verify(cacheServiceMock, times(1)).invalidateGoalsCaches();
    }

    @Test
    void shouldThrowGoalNotFoundExceptionWhenDeletingGoal() {
        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.empty());

        var exception = assertThrows(
            GoalNotFoundException.class,
            () -> goalService.deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3")
        );

        verify(goalRepositoryMock, never()).deleteById("10e3c7c1-0732-4294-9410-9708a21128e3");
        assertEquals(GOAL_ID_NOT_FOUND.formatted("10e3c7c1-0732-4294-9410-9708a21128e3"), exception.getMessage());
    }

}
