package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.request.usercrypto.UserCryptoRequest;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.PageUserCryptoResponse;
import com.distasilucas.cryptobalancetracker.model.response.usercrypto.UserCryptoResponse;
import com.distasilucas.cryptobalancetracker.service.UserCryptoService;
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

import static com.distasilucas.cryptobalancetracker.TestDataSource.deleteUserCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getFileContent;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveUserCryptoById;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveUserCryptosForPage;
import static com.distasilucas.cryptobalancetracker.TestDataSource.saveUserCrypto;
import static com.distasilucas.cryptobalancetracker.TestDataSource.updateUserCrypto;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_SIZE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_DECIMAL_MAX;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_NOT_NULL;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_QUANTITY_POSITIVE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebMvcTest(UserCryptoController.class)
class UserCryptoControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserCryptoService userCryptoServiceMock;

    @Test
    void shouldRetrieveUserCryptoByIdWithStatus200() throws Exception {
        var userCrypto = getUserCrypto();
        var userCryptoResponse = userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE");

        when(userCryptoServiceMock.retrieveUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d"))
                .thenReturn(userCryptoResponse);

        mockMvc.perform(retrieveUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("af827ac7-d642-4461-a73c-b31ca6f6d13d")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.quantity", is("0.25")))
                .andExpect(jsonPath("$.platform", is("BINANCE")));
    }

    @Test
    void shouldRetrieveUserCryptoWithMaxValueWithStatus200() throws Exception {
        var userCrypto = new UserCrypto(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "bitcoin",
                new BigDecimal("9999999999999999.999999999999"),
                "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
        );

        var userCryptoResponse = userCrypto.toUserCryptoResponse("Bitcoin", "BINANCE");

        when(userCryptoServiceMock.retrieveUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d"))
                .thenReturn(userCryptoResponse);

        mockMvc.perform(retrieveUserCryptoById("af827ac7-d642-4461-a73c-b31ca6f6d13d"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("af827ac7-d642-4461-a73c-b31ca6f6d13d")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.quantity", is("9999999999999999.999999999999")))
                .andExpect(jsonPath("$.platform", is("BINANCE")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "af827ac7-d642-4461-a73c-b31ca6f6d13", "af827a7-d642-4461-a73c-b31ca6f6d13d",
            "af827ac7-d42-4461-a73c-b31ca6f6d13d", "af827ac7-d642-4461a73c-b31ca6f6d13d"
    })
    void shouldFailWithStatus400WithOneMessageWhenRetrievingUserCryptoWithInvalidId(String userCryptoId) throws Exception {
        mockMvc.perform(retrieveUserCryptoById(userCryptoId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("User crypto id must be a valid UUID")));
    }

    @Test
    void shouldRetrieveUserCryptosForPageWithStatus200() throws Exception {
        var userCryptoResponse = new UserCryptoResponse(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "Bitcoin",
                "0.5",
                "Binance"
        );

        var pageUserCryptoResponse = new PageUserCryptoResponse(1, 1, false, List.of(userCryptoResponse));

        when(userCryptoServiceMock.retrieveUserCryptosByPage(0)).thenReturn(pageUserCryptoResponse);

        mockMvc.perform(retrieveUserCryptosForPage(0))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.totalPages", is(1)))
                .andExpect(jsonPath("$.hasNextPage", is(false)))
                .andExpect(jsonPath("$.cryptos").isArray())
                .andExpect(jsonPath("$.cryptos", hasSize(1)))
                .andExpect(jsonPath("$.cryptos.[0].id", is("af827ac7-d642-4461-a73c-b31ca6f6d13d")))
                .andExpect(jsonPath("$.cryptos.[0].cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.cryptos.[0].platform", is("Binance")))
                .andExpect(jsonPath("$.cryptos.[0].quantity", is("0.5")));
    }

    @Test
    void shouldReturnEmptyUserCryptosForPageWithStatus204() throws Exception {
        var pageUserCryptoResponse = new PageUserCryptoResponse(5, 5, emptyList());

        when(userCryptoServiceMock.retrieveUserCryptosByPage(5)).thenReturn(pageUserCryptoResponse);

        mockMvc.perform(retrieveUserCryptosForPage(5))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenRetrievingUserCryptosWithInvalidPage() throws Exception {
        mockMvc.perform(retrieveUserCryptosForPage(-1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(INVALID_PAGE_NUMBER)));
    }

    @Test
    void shouldSaveUserCryptoWithStatus200() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        var userCryptoResponse = new UserCryptoResponse("af827ac7-d642-4461-a73c-b31ca6f6d13d", "Bitcoin", "1", "Binance");

        when(userCryptoServiceMock.saveUserCrypto(
                new UserCryptoRequest("bitcoin", new BigDecimal(1), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")
        )).thenReturn(userCryptoResponse);

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("af827ac7-d642-4461-a73c-b31ca6f6d13d")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.quantity", is("1")))
                .andExpect(jsonPath("$.platform", is("Binance")));
    }

    @Test
    void shouldSaveUserCryptoWithStatus200WithMaxQuantity() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("9999999999999999.999999999999"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        var userCryptoResponse = new UserCryptoResponse(
                "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                "Bitcoin",
                "9999999999999999.999999999999",
                "Binance"
        );

        when(userCryptoServiceMock.saveUserCrypto(
                new UserCryptoRequest("bitcoin", new BigDecimal("9999999999999999.999999999999"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")
        )).thenReturn(userCryptoResponse);

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("af827ac7-d642-4461-a73c-b31ca6f6d13d")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.quantity", is("9999999999999999.999999999999")))
                .andExpect(jsonPath("$.platform", is("Binance")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingUserCryptoWithBlankCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(" ", new BigDecimal("9999999999999999.999999999999"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_NOT_BLANK)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingUserCryptoWithNullCryptoName() throws Exception {
        var content = """
                {
                    "cryptoName": null,
                    "quantity": "1",
                    "platformId": "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
                }
                """;

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_NOT_BLANK)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingUserCryptoWithLongCryptoName() throws Exception {
        var longName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName";
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(longName, new BigDecimal("9999999999999999.999999999999"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_SIZE)));
    }

    @Test
    void shouldFailWithStatus400WithThreeMessagesWhenSavingUserCryptoWithZeroSizeCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("", new BigDecimal("9999999999999999.999999999999"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(
                        jsonPath("$[*].detail").value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE))
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {" bitcoin", "bitcoin ", "bit  coin", "bit!coin"})
    void shouldFailWithStatus400WithOneMessageWhenSavingUserCryptoWithInvalidCryptoName(String cryptoName) throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(cryptoName, new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingUserCryptoWithNullQuantity() throws Exception {
        var content = """
                {
                    "cryptoName": "bitcoin",
                    "quantity": null,
                    "platformId": "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
                }
                """;

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(CRYPTO_QUANTITY_NOT_NULL)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999.999999999999", "9999999999999999.9999999999999"})
    void shouldFailWithStatus400WithTwoMessagesWhenSavingUserCryptoWithInvalidQuantity(String quantity) throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal(quantity), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(
                                containsInAnyOrder(
                                        CRYPTO_QUANTITY_DECIMAL_MAX,
                                        "Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                                )
                        )
                );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingUserCryptoWithLessThanMinimumQuantity() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("0.0000000000001"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath(
                                "$[0].detail",
                                is("Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                        )
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "-100", "0"})
    void shouldFailWithStatus400WithOneMessageWhenSavingUserCryptoWithNegativeQuantity(String quantity) throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal(quantity), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(CRYPTO_QUANTITY_POSITIVE)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingUserCryptoWithBlankPlatformId() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("1"), "");

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(PLATFORM_ID_NOT_BLANK, PLATFORM_ID_UUID)));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingUserCryptoWithNullPlatformId() throws Exception {
        var content = """
                {
                    "cryptoName": "bitcoin",
                    "quantity": 1,
                    "platformId": null
                }
                """;

        mockMvc.perform(saveUserCrypto(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(PLATFORM_ID_NOT_BLANK)));
    }

    @Test
    void shouldUpdateUserCryptoWithStatus200() throws Exception {
        var cryptoName = "bitcoin";
        var quantity = new BigDecimal("9999999999999999.999999999999");
        var platformId = "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6";
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(cryptoName, quantity, platformId);
        var userCryptoRequest = new UserCryptoRequest(cryptoName, quantity, platformId);

        var userCryptoResponse = new UserCryptoResponse(
                "123e4567-e89b-12d3-a456-426614174222",
                "Bitcoin",
                "9999999999999999.999999999999",
                "Binance"
        );

        when(userCryptoServiceMock.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest))
                .thenReturn(userCryptoResponse);

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("123e4567-e89b-12d3-a456-426614174222")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.quantity", is("9999999999999999.999999999999")))
                .andExpect(jsonPath("$.platform", is("Binance")));
    }

    @Test
    void shouldUpdateUserCryptoWithStatus200WithMaxQuantity() throws Exception {
        var cryptoName = "bitcoin";
        var quantity = new BigDecimal("9999999999999999.999999999999");
        var platformId = "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6";
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(cryptoName, quantity, platformId);
        var userCryptoRequest = new UserCryptoRequest(cryptoName, quantity, platformId);
        var userCryptoResponse = new UserCryptoResponse(
                "123e4567-e89b-12d3-a456-426614174222", "Bitcoin", "9999999999999999.999999999999", "Binance"
        );

        when(userCryptoServiceMock.updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", userCryptoRequest))
                .thenReturn(userCryptoResponse);

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("123e4567-e89b-12d3-a456-426614174222")))
                .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
                .andExpect(jsonPath("$.quantity", is("9999999999999999.999999999999")))
                .andExpect(jsonPath("$.platform", is("Binance")));
    }

    @Test
    void shouldFailWhenUpdatingUserCryptoWithInvalidUserCryptoId() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-42661417422", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("User crypto id must be a valid UUID")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingUserCryptoWithBlankCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(" ", new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_NOT_BLANK)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingUserCryptoWithNullCryptoName() throws Exception {
        var content = """
                {
                    "cryptoName": null,
                    "quantity": 1,
                    "platformId": "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
                }
                """;

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_NOT_BLANK)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWwhenUpdatingUserCryptoWwithLongCryptoName() throws Exception {
        var cryptoName = "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName";
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(cryptoName, new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_SIZE)));
    }

    @Test
    void shouldFailWithStatus400WithThreeMessagesWhenUpdatingUserCryptoWithZeroSizeCryptoName() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("", new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder("Invalid crypto name", CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {" bitcoin", "bitcoin ", "bit  coin", "bit!coin"})
    void shouldFailWithStatus400WithOneMessageWhenUpdatingUserCryptoWithInvalidCryptoName(String cryptoName) throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted(cryptoName, new BigDecimal("1"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is("Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingUserCryptoWithNullQuantity() throws Exception {
        var content = """
                {
                    "cryptoName": "bitcoin",
                    "quantity": null,
                    "platformId": "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"
                }
                """;

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(CRYPTO_QUANTITY_NOT_NULL)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999.999999999999", "9999999999999999.9999999999999"})
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingUserCryptoWithInvalidQuantity(String quantity) throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal(quantity), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail")
                        .value(containsInAnyOrder(
                                CRYPTO_QUANTITY_DECIMAL_MAX,
                                "Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                        )));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingUserCryptoWithInvalidQuantity() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("0.0000000000001"), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath(
                                "$[0].detail",
                                is("Crypto quantity must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                        )
                );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "-100", "0"})
    void shouldFailWithStatus400WithOneMessageWhenUpdatingUserCryptoWithNegativeQuantity(String quantity) throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal(quantity), "4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(CRYPTO_QUANTITY_POSITIVE)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingUserCryptoWithBlankPlatformId() throws Exception {
        var content = getFileContent("request/platform/save_update_user_crypto.json")
                .formatted("bitcoin", new BigDecimal("1"), "");

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail")
                    .value(containsInAnyOrder(PLATFORM_ID_NOT_BLANK, PLATFORM_ID_UUID))
            );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingUserCryptoWithNullPlatformId() throws Exception {
        var content = """
        {
            "cryptoName": "bitcoin",
            "quantity": 1,
            "platformId": null
        }
        """;

        mockMvc.perform(updateUserCrypto("123e4567-e89b-12d3-a456-426614174222", content))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is(PLATFORM_ID_NOT_BLANK)));
    }

    @Test
    void shouldDeleteUserCryptoWithStatus200() throws Exception {
        doNothing().when(userCryptoServiceMock).deleteUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d");

        mockMvc.perform(deleteUserCrypto("af827ac7-d642-4461-a73c-b31ca6f6d13d"))
            .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "af827ac7-d642-4461-a73c-b31ca6f6d13", "af827a7-d642-4461-a73c-b31ca6f6d13d",
            "af827ac7-d42-4461-a73c-b31ca6f6d13d", "af827ac7-d642-4461a73c-b31ca6f6d13d"
    })
    void shouldFailWithStatus400WithOneMessageWhenDeletingUserCryptoWithInvalidId(String userCryptoId) throws Exception {
        mockMvc.perform(deleteUserCrypto(userCryptoId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("User crypto id must be a valid UUID")));
    }

}
