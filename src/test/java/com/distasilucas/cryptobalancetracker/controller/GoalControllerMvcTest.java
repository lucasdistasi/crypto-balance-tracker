package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.request.goal.GoalRequest;
import com.distasilucas.cryptobalancetracker.model.response.goal.PageGoalResponse;
import com.distasilucas.cryptobalancetracker.service.GoalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.TestDataSource.deleteGoal;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getFileContent;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalRequest;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getGoalResponse;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveGoalById;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveGoalsForPage;
import static com.distasilucas.cryptobalancetracker.TestDataSource.saveGoal;
import static com.distasilucas.cryptobalancetracker.TestDataSource.updateGoal;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_SIZE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_GOAL_UUID;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@WebMvcTest(GoalController.class)
class GoalControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GoalService goalServiceMock;

    @Test
    void shouldRetrieveGoalWithStatus200() throws Exception {
        var goalResponse = getGoalResponse();

        when(goalServiceMock.retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3")).thenReturn(goalResponse);

        mockMvc.perform(retrieveGoalById("10e3c7c1-0732-4294-9410-9708a21128e3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("10e3c7c1-0732-4294-9410-9708a21128e3")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.actualQuantity", is("1")))
                .andExpect(jsonPath("$.progress", is(100.0)))
                .andExpect(jsonPath("$.remainingQuantity", is("0")))
                .andExpect(jsonPath("$.goalQuantity", is("1")))
                .andExpect(jsonPath("$.moneyNeeded", is("0")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "10e3c7c1-0732-4294-9410-9708a21128e", "103c7c1-0732-4294-9410-9708a21128e3",
            "10e3c7c1-07324294-9410-9708a21128e3", "10e3c7c1-0732-4294-94109708a21128e3"
    })
    void shouldFailWithStatus400WithOneMessageWhenRetrievingGoalWwithInvalidId(String goalId) throws Exception {
        mockMvc.perform(retrieveGoalById(goalId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Goal id must be a valid UUID")));
    }

    @Test
    void shouldRetrieveGoalsForPageWithStatus200() throws Exception {
        var pageGoalResponse = new PageGoalResponse(1, 1, false, List.of(getGoalResponse()));

        when(goalServiceMock.retrieveGoalsForPage(0)).thenReturn(pageGoalResponse);

        mockMvc.perform(retrieveGoalsForPage(0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals", hasSize(1)))
                .andExpect(jsonPath("$.goals[0].id", is("10e3c7c1-0732-4294-9410-9708a21128e3")))
                .andExpect(jsonPath("$.goals[0].cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.goals[0].actualQuantity", is("1")))
                .andExpect(jsonPath("$.goals[0].progress", is(100.0)))
                .andExpect(jsonPath("$.goals[0].remainingQuantity", is("0")))
                .andExpect(jsonPath("$.goals[0].goalQuantity", is("1")))
                .andExpect(jsonPath("$.goals[0].moneyNeeded", is("0")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenRetrievingGoalsWithInvalidPage() throws Exception {
        mockMvc.perform(retrieveGoalsForPage(-1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(INVALID_PAGE_NUMBER)));
    }

    @Test
    void shouldSaveGoalWithStatus200() throws Exception {
        var goalRequest = getGoalRequest();
        var goalResponse = getGoalResponse();
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("1"));

        when(goalServiceMock.saveGoal(goalRequest)).thenReturn(goalResponse);

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("10e3c7c1-0732-4294-9410-9708a21128e3")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.actualQuantity", is("1")))
                .andExpect(jsonPath("$.progress", is(100.0)))
                .andExpect(jsonPath("$.remainingQuantity", is("0")))
                .andExpect(jsonPath("$.goalQuantity", is("1")))
                .andExpect(jsonPath("$.moneyNeeded", is("0")));
    }

    @Test
    void shouldSaveGoalWithMaxQuantityWithStatus200() throws Exception {
        var goalResponse = getGoalResponse();
        var goalRequest = new GoalRequest("bitcoin", new BigDecimal("9999999999999999.999999999999"));
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("9999999999999999.999999999999"));

        when(goalServiceMock.saveGoal(goalRequest)).thenReturn(goalResponse);

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("10e3c7c1-0732-4294-9410-9708a21128e3")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.actualQuantity", is("1")))
                .andExpect(jsonPath("$.progress", is(100.0)))
                .andExpect(jsonPath("$.remainingQuantity", is("0")))
                .andExpect(jsonPath("$.goalQuantity", is("1")))
                .andExpect(jsonPath("$.moneyNeeded", is("0")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingGoalWithBlankCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted(" ", new BigDecimal("1"));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name"))
                );
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingGoalWithEmptyCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("", new BigDecimal("1"));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE, "Invalid crypto name"))
                );
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingGoalWithLongCryptoName() throws Exception {
        var longCryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName";
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted(longCryptoName, new BigDecimal("1"));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_SIZE, "Invalid crypto name"))
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {" bitcoin", "bitcoin ", "bit  coin", "bit!coin"})
    void shouldFailWithStatus400WithOneMessageWhenSavingGoalWithInvalidCryptoName(String cryptoName) throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted(cryptoName, new BigDecimal("1"));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingGoalWithNullCryptoName() throws Exception {
        var content = """
                    {
                        "cryptoName": null,
                        "goalQuantity": 1
                    }
                """;

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name"))
                );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingGoalWithNullGoalQuantity() throws Exception {
        var content = """
                    {
                        "cryptoName": "bitcoin",
                        "goalQuantity": null
                    }
                """;

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Goal quantity can not be null")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999.999999999999", "9999999999999999.9999999999999"})
    void shouldFailWithStatus400WithTwoMessagesWhenSavingGoalWithInvalidGoalQuantity(String goalQuantity) throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal(goalQuantity));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(
                        jsonPath("$[*].detail")
                                .value(
                                        containsInAnyOrder(
                                                "Goal quantity must be less than or equal to 9999999999999999.999999999999",
                                                "Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                                        )
                                )
                );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingGoalWithInvalidGoalQuantity() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("0.0000000000001"));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath(
                                "$[0].detail",
                                is("Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                        )
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "-100", "0"})
    void shouldFailWithStatus400WithOneMessageWhenSavingGoalWithNegativeGoalQuantity(String goalQuantity) throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal(goalQuantity));

        mockMvc.perform(saveGoal(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Goal quantity must be greater than 0")));
    }

    @Test
    void shouldUpdateGoalWithStatus200() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("1"));
        var goalResponse = getGoalResponse();
        var goalRequest = new GoalRequest("bitcoin", new BigDecimal("1"));

        when(goalServiceMock.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest)).thenReturn(goalResponse);

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("10e3c7c1-0732-4294-9410-9708a21128e3")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.actualQuantity", is("1")))
                .andExpect(jsonPath("$.progress", is(100.0)))
                .andExpect(jsonPath("$.remainingQuantity", is("0")))
                .andExpect(jsonPath("$.goalQuantity", is("1")))
                .andExpect(jsonPath("$.moneyNeeded", is("0")));
    }

    @Test
    void shouldUpdateGoalWithMaxQuantityWithStatus200() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("9999999999999999.999999999999"));
        var goalRequest = new GoalRequest("bitcoin", new BigDecimal("9999999999999999.999999999999"));
        var goalResponse = getGoalResponse();

        when(goalServiceMock.updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", goalRequest)).thenReturn(goalResponse);

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("10e3c7c1-0732-4294-9410-9708a21128e3")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.actualQuantity", is("1")))
                .andExpect(jsonPath("$.progress", is(100.0)))
                .andExpect(jsonPath("$.remainingQuantity", is("0")))
                .andExpect(jsonPath("$.goalQuantity", is("1")))
                .andExpect(jsonPath("$.moneyNeeded", is("0")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingGoalWithBlankCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted(" ", new BigDecimal("1"));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name"))
                );
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingGoalWithEmptyCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("", new BigDecimal("1"));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE, "Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingGoalWithLongCryptoName() throws Exception {
        var longCryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName";
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted(longCryptoName, new BigDecimal("1"));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_SIZE, "Invalid crypto name")));
    }

    @ParameterizedTest
    @ValueSource(strings = {" bitcoin", "bitcoin ", "bit  coin", "bit!coin"})
    void shouldFailWithStatus400WithOneMessageWhenUpdatingGoalWithInvalidCryptoName(String cryptoName) throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted(cryptoName, new BigDecimal("1"));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingGoalWithNullCryptoName() throws Exception {
        var content = """
                    {
                        "cryptoName": null,
                        "goalQuantity": 1
                    }
                """;

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingGoalWithNullGoalQuantity() throws Exception {
        var content = """
                    {
                        "cryptoName": "bitcoin",
                        "goalQuantity": null
                    }
                """;

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Goal quantity can not be null")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999.999999999999", "9999999999999999.9999999999999"})
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingGoalWithInvalidGoalQuantity(String goalQuantity) throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal(goalQuantity));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(
                                containsInAnyOrder(
                                        "Goal quantity must be less than or equal to 9999999999999999.999999999999",
                                        "Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                                )
                        ));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingGoalWithInvalidGoalQuantity() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("0.0000000000001"));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath(
                        "$[0].detail",
                        is("Goal quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "-100", "0"})
    void shouldFailWithStatus400WithOneMessageWhenUpdatingGoalWithNegativeGoalQuantity(String goalQuantity) throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal(goalQuantity));

        mockMvc.perform(updateGoal("10e3c7c1-0732-4294-9410-9708a21128e3", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Goal quantity must be greater than 0")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingGoalWithInvalidGoalId() throws Exception {
        var content = getFileContent("request/platform/save_update_goal.json")
                .formatted("bitcoin", new BigDecimal("1"));

        mockMvc.perform(updateGoal("123e4567-e89b-12d3-a456-42661417411", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(INVALID_GOAL_UUID)));
    }

    @Test
    void shouldDeleteGoal() throws Exception {
        doNothing().when(goalServiceMock).deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3");

        mockMvc.perform(deleteGoal("10e3c7c1-0732-4294-9410-9708a21128e3"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "10e3c7c1-0732-4294-9410-9708a21128e", "103c7c1-0732-4294-9410-9708a21128e3",
            "10e3c7c1-07324294-9410-9708a21128e3", "10e3c7c1-0732-4294-94109708a21128e3"
    })
    void shouldFailWithStatus400WithOneMessageWhenDeletingGoalWithInvalidGoalId(String goalId) throws Exception {
        mockMvc.perform(deleteGoal(goalId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(INVALID_GOAL_UUID)));
    }

}
