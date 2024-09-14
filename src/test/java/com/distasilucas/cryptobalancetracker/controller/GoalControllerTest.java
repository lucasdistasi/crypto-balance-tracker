package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import com.distasilucas.cryptobalancetracker.model.response.goal.GoalResponse;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.service.GoalService;
import com.distasilucas.cryptobalancetracker.service.UserCryptoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoal;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalRequest;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getPageGoal;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class GoalControllerTest {

    @Mock
    private GoalService goalServiceMock;

    @Mock
    private UserCryptoService userCryptoServiceMock;

    private GoalController goalController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        goalController = new GoalController(goalServiceMock, userCryptoServiceMock);
    }

    @Test
    void shouldRetrieveGoalWithStatus200() {
        var userCryptos = List.of(getUserCrypto());

        when(goalServiceMock.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(getGoal());
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(userCryptos);

        var responseEntity = goalController.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(responseEntity)
            .usingRecursiveAssertion()
            .isEqualTo(ResponseEntity.ok(
                new GoalResponse(
                    "10e3c7c1-0732-4294-9410-9708a21128e3",
                    "Bitcoin",
                    "0.25",
                    25F,
                    "0.75",
                    "1",
                    "22500.00"
                )
            ));
    }

    @Test
    void shouldRetrieveGoalsForPageWithStatus200() {
        var goalResponse = new GoalResponse("10e3c7c1-0732-4294-9410-9708a21128e3", "Bitcoin", "0.25", 25F, "0.75", "1", "22500.00");
        var pageGoalResponse = new PageGoalResponse(0, 1, List.of(goalResponse));
        var userCryptos = List.of(getUserCrypto());

        when(goalServiceMock.retrieveGoalsForPage(0)).thenReturn(getPageGoal());
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(userCryptos);

        var responseEntity = goalController.retrieveGoalsForPage(0);

        assertThat(responseEntity)
            .usingRecursiveAssertion()
            .isEqualTo(ResponseEntity.ok(pageGoalResponse));
    }

    @Test
    void shouldRetrieveGoalsForPageWithStatus200AndNextPage() {
        var goalResponse = new GoalResponse("10e3c7c1-0732-4294-9410-9708a21128e3", "Bitcoin", "0.25", 25F, "0.75", "1", "22500.00");
        var pageGoalResponse = new PageGoalResponse(1, 2, true, List.of(goalResponse));
        var userCryptos = List.of(getUserCrypto());
        var goalsPage = new PageImpl<>(List.of(getGoal()), PageRequest.of(0, 10), 20);

        when(goalServiceMock.retrieveGoalsForPage(0)).thenReturn(goalsPage);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(userCryptos);

        var responseEntity = goalController.retrieveGoalsForPage(0);

        assertThat(responseEntity)
            .usingRecursiveAssertion()
            .isEqualTo(ResponseEntity.ok(pageGoalResponse));
    }

    @Test
    void shouldRetrieveGoalsForPageWithStatus204() {
        when(goalServiceMock.retrieveGoalsForPage(0)).thenReturn(Page.empty());

        var responseEntity = goalController.retrieveGoalsForPage(0);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldSaveGoalAndReturn200() {
        var goalRequest = getGoalRequest();
        var goal = new Goal("22d68987-471a-4dbf-8bb1-e830ea1a1aaa", new BigDecimal("1"), getBitcoinCryptoEntity());
        var userCryptos = List.of(getUserCrypto());
        var goalResponse = goal.toGoalResponse(userCryptos);

        when(goalServiceMock.saveGoal(goalRequest)).thenReturn(goal);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(userCryptos);

        var responseEntity = goalController.saveGoal(goalRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(goalResponse));
    }

    @Test
    void shouldUpdateGoalAndReturn200() {
        var goalRequest = getGoalRequest();
        var goal = new Goal("22d68987-471a-4dbf-8bb1-e830ea1a1aaa", new BigDecimal("1"), getBitcoinCryptoEntity());
        var userCryptos = List.of(getUserCrypto());

        when(goalServiceMock.updateGoal("22d68987-471a-4dbf-8bb1-e830ea1a1aaa", goalRequest)).thenReturn(goal);
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(userCryptos);

        var responseEntity = goalController.updateGoal("22d68987-471a-4dbf-8bb1-e830ea1a1aaa", goalRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(
                new GoalResponse(
                    "22d68987-471a-4dbf-8bb1-e830ea1a1aaa",
                    "Bitcoin",
                    "0.25",
                    25F,
                    "0.75",
                    "1",
                    "22500.00"
                )
            ));
    }

    @Test
    void shouldDeleteGoalAndReturn200() {
        doNothing().when(goalServiceMock).deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        var responseEntity = goalController.deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

}
