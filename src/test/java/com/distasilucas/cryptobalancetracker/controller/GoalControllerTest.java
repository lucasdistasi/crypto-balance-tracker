package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.service.GoalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalRequest;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalResponse;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getPageGoalResponse;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class GoalControllerTest {

    @Mock
    private GoalService goalServiceMock;

    private GoalController goalController;

    @BeforeEach
    void setUp() {
        openMocks(this);
        goalController = new GoalController(goalServiceMock);
    }

    @Test
    void shouldRetrieveGoalWithStatus200() {
        var goalResponse = getGoalResponse();

        when(goalServiceMock.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3"))
            .thenReturn(goalResponse);

        var responseEntity = goalController.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(responseEntity)
            .usingRecursiveAssertion()
            .isEqualTo(ResponseEntity.ok(goalResponse));
    }

    @Test
    void shouldRetrieveGoalsForPageWithStatus200() {
        var pageGoalResponse = getPageGoalResponse();

        when(goalServiceMock.retrieveGoalsForPage(0)).thenReturn(pageGoalResponse);

        var responseEntity = goalController.retrieveGoalsForPage(0);

        assertThat(responseEntity)
            .usingRecursiveAssertion()
            .isEqualTo(ResponseEntity.ok(pageGoalResponse));
    }

    @Test
    void shouldRetrieveGoalsForPageWithStatus204() {
        var pageGoalResponse = new PageGoalResponse(1, 1, false, emptyList());

        when(goalServiceMock.retrieveGoalsForPage(0)).thenReturn(pageGoalResponse);

        var responseEntity = goalController.retrieveGoalsForPage(0);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.noContent().build());
    }

    @Test
    void shouldSaveGoalAndReturn200() {
        var goalRequest = getGoalRequest();
        var goalResponse = getGoalResponse();

        when(goalServiceMock.saveGoal(goalRequest)).thenReturn(goalResponse);

        var responseEntity = goalController.saveGoal(goalRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(goalResponse));
    }

    @Test
    void shouldUpdateGoalAndReturn200() {
        var goalRequest = getGoalRequest();
        var goalResponse = getGoalResponse();

        when(goalServiceMock.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest))
            .thenReturn(goalResponse);

        var responseEntity = goalController.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest);

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok(goalResponse));
    }

    @Test
    void shouldDeleteGoalAndReturn200() {
        doNothing().when(goalServiceMock).deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        var responseEntity = goalController.deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        assertThat(responseEntity)
            .usingRecursiveComparison()
            .isEqualTo(ResponseEntity.ok().build());
    }

}
