package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.exception.DuplicatedGoalException;
import com.distasilucas.cryptobalancetracker.exception.GoalNotFoundException;
import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getCoingeckoCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalRequest;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.DUPLICATED_GOAL;
import static com.distasilucas.cryptobalancetracker.constants.ExceptionConstants.GOAL_ID_NOT_FOUND;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
    private UserCryptoService userCryptoServiceMock;

    private GoalService goalService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        goalService = new GoalService(goalRepositoryMock, cryptoServiceMock, userCryptoServiceMock);
    }

    @Test
    void shouldRetrieveGoalById() {
        var goalEntity = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("1"));
        var cryptoEntity = getBitcoinCryptoEntity();
        var userCrypto = getUserCrypto();
        var expected = new GoalResponse("10e3c7c1-0732-4294-9410-9708a21128e3", "Bitcoin", "0.25", 25f, "0.75", "1", "22500.00");

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goalEntity));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(cryptoEntity);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var goalResponse = goalService.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(goalResponse)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    @Test
    void shouldRetrieveCompletedGoal() {
        var goalEntity = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("1"));
        var cryptoEntity = getBitcoinCryptoEntity();
        var userCrypto = new UserCrypto(
            "af827ac7-d642-4461-a73c-b31ca6f6d13d",
            "bitcoin",
            new BigDecimal("1"),
            "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        );
        var expected = new GoalResponse("10e3c7c1-0732-4294-9410-9708a21128e3", "Bitcoin", "1", 100f, "0", "1", "0.00");

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goalEntity));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(cryptoEntity);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var goalResponse = goalService.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(goalResponse)
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
        var goal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("1"));
        var cryptoEntity = getBitcoinCryptoEntity();
        var userCrypto = getUserCrypto();

        when(goalRepositoryMock.findAll(pageRequest)).thenReturn(new PageImpl<>(List.of(goal)));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(cryptoEntity);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var goalsResponse = goalService.retrieveGoalsForPage(0);

        assertThat(goalsResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new PageGoalResponse(1, 1, false,
                    List.of(
                        new GoalResponse(
                            "10e3c7c1-0732-4294-9410-9708a21128e3",
                            "Bitcoin",
                            "0.25",
                            25f,
                            "0.75",
                            "1",
                            "22500.00"
                        ))
                )
            );
    }

    @Test
    void shouldRetrieveGoalsForPageWithNextPage() {
        var goal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("1"));
        var cryptoEntity = getBitcoinCryptoEntity();
        var userCrypto = getUserCrypto();
        var goalPage = List.of(goal, goal);
        var pageImpl = new PageImpl<>(goalPage, PageRequest.of(0, 2), 10L);

        when(goalRepositoryMock.findAll(PageRequest.of(0, 10))).thenReturn(pageImpl);
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(cryptoEntity);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var goalsResponse = goalService.retrieveGoalsForPage(0);

        assertThat(goalsResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new PageGoalResponse(1, 5, true,
                    List.of(
                        new GoalResponse(
                            "10e3c7c1-0732-4294-9410-9708a21128e3",
                            "Bitcoin",
                            "0.25",
                            25f,
                            "0.75",
                            "1",
                            "22500.00"
                        ),
                        new GoalResponse(
                            "10e3c7c1-0732-4294-9410-9708a21128e3",
                            "Bitcoin",
                            "0.25",
                            25f,
                            "0.75",
                            "1",
                            "22500.00"
                        )
                    )
                )
            );
    }

    @Test
    void shouldRetrieveEmptyGoalsForPage() {
        var pageRequest = PageRequest.of(0, 10);

        when(goalRepositoryMock.findAll(pageRequest)).thenReturn(Page.empty());

        var goalsResponse = goalService.retrieveGoalsForPage(0);

        assertFalse(goalsResponse.hasNextPage());
        assertThat(goalsResponse)
            .usingRecursiveComparison()
            .isEqualTo(new PageGoalResponse(1, 1, false, emptyList()));
    }

    @Test
    void shouldSaveGoal() {
        var goalRequest = getGoalRequest();
        var coingeckoCrypto = getCoingeckoCrypto();
        var cryptoEntity = getBitcoinCryptoEntity();
        var userCrypto = getUserCrypto();

        var captor = ArgumentCaptor.forClass(Goal.class);

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
        when(goalRepositoryMock.findByCoingeckoCryptoId("bitcoin")).thenReturn(Optional.empty());
        doNothing().when(cryptoServiceMock).saveCryptoIfNotExists("bitcoin");
        when(goalRepositoryMock.save(captor.capture())).thenAnswer(answer -> captor.getValue());
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(cryptoEntity);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));

        var goalResponse = goalService.saveGoal(goalRequest);

        verify(goalRepositoryMock, times(1)).save(captor.getValue());
        assertThat(goalResponse)
            .usingRecursiveComparison()
            .isEqualTo(
                new GoalResponse(captor.getValue().id(), "Bitcoin", "0.25", 25f, "0.75", "1", "22500.00")
            );
    }

    @Test
    void shouldThrowDuplicatedGoalExceptionWhenSavingGoal() {
        var goalRequest = getGoalRequest();
        var existingGoal = getGoalEntity();
        var coingeckoCrypto = getCoingeckoCrypto();

        when(cryptoServiceMock.retrieveCoingeckoCryptoInfoByName("bitcoin")).thenReturn(coingeckoCrypto);
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
        var goal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("1"));
        var updatedGoal = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("0.75"));
        var cryptoEntity = getBitcoinCryptoEntity();
        var userCrypto = getUserCrypto();
        var expected = new GoalResponse("10e3c7c1-0732-4294-9410-9708a21128e3", "Bitcoin", "0.25", 33.33f, "0.50", "0.75", "15000.00");

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goal));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(cryptoEntity);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(userCrypto));
        when(goalRepositoryMock.save(updatedGoal)).thenReturn(updatedGoal);

        var goalResponse = goalService.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest);

        assertThat(goalResponse)
            .usingRecursiveComparison()
            .isEqualTo(expected);
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
        var goalEntity = new Goal("10e3c7c1-0732-4294-9410-9708a21128e3", "bitcoin", new BigDecimal("1"));

        when(goalRepositoryMock.findById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(Optional.of(goalEntity));
        doNothing().when(goalRepositoryMock).deleteById("10e3c7c1-0732-4294-9410-9708a21128e3");
        doNothing().when(cryptoServiceMock).deleteCryptoIfNotUsed("bitcoin");

        goalService.deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        verify(goalRepositoryMock, times(1)).deleteById("10e3c7c1-0732-4294-9410-9708a21128e3");
        verify(cryptoServiceMock, times(1)).deleteCryptoIfNotUsed("bitcoin");
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
