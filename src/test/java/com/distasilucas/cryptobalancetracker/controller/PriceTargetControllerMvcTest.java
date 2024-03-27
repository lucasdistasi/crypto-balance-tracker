package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.request.pricetarget.PriceTargetRequest;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PagePriceTargetResponse;
import com.distasilucas.cryptobalancetracker.model.response.pricetarget.PriceTargetResponse;
import com.distasilucas.cryptobalancetracker.service.PriceTargetService;
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

import static com.distasilucas.cryptobalancetracker.TestDataSource.deletePriceTarget;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrievePriceTargetById;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrievePriceTargetsForPage;
import static com.distasilucas.cryptobalancetracker.TestDataSource.savePriceTarget;
import static com.distasilucas.cryptobalancetracker.TestDataSource.updatePriceTarget;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_NOT_BLANK;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.CRYPTO_NAME_SIZE;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PAGE_NUMBER;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PRICE_TARGET_UUID;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@WebMvcTest(PriceTargetController.class)
class PriceTargetControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceTargetService priceTargetServiceMock;

    @Test
    void shouldRetrievePriceTargetWithStatus200() throws Exception {
        var priceTargetResponse = getPriceTargetResponse();

        when(priceTargetServiceMock.retrievePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5"))
            .thenReturn(priceTargetResponse);

        mockMvc.perform(retrievePriceTargetById("2ca0a475-bf4b-4733-9f13-6be497ad6fe5"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priceTargetId", is("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
            .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.currentPrice", is("60000")))
            .andExpect(jsonPath("$.priceTarget", is("100000")))
            .andExpect(jsonPath("$.change", is(35.30)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
        "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    })
    void shouldFailWithStatus400WithOneMessageWhenRetrievingPriceTargetWithInvalidId(String priceTargetId) throws Exception {
        mockMvc.perform(retrievePriceTargetById(priceTargetId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Price target id must be a valid UUID")));
    }

    @Test
    void shouldRetrievePriceTargetsForPageWithStatus200() throws Exception {
        var pagePriceTargetResponse = new PagePriceTargetResponse(1, 1, false, List.of(getPriceTargetResponse()));

        when(priceTargetServiceMock.retrievePriceTargetsByPage(0)).thenReturn(pagePriceTargetResponse);

        mockMvc.perform(retrievePriceTargetsForPage(0))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.targets").isArray())
            .andExpect(jsonPath("$.targets", hasSize(1)))
            .andExpect(jsonPath("$.targets[0].priceTargetId", is("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
            .andExpect(jsonPath("$.targets[0].cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.targets[0].currentPrice", is("60000")))
            .andExpect(jsonPath("$.targets[0].priceTarget", is("100000")))
            .andExpect(jsonPath("$.targets[0].change", is(35.30)));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenRetrievingPriceTargetsWithInvalidPage() throws Exception {
        mockMvc.perform(retrievePriceTargetsForPage(-1))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is(INVALID_PAGE_NUMBER)));
    }

    @Test
    void shouldSavePriceTargetWithStatus200() throws Exception {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var priceTargetResponse = getPriceTargetResponse();
        var payload = """
              {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": 100000
              }
            """;

        when(priceTargetServiceMock.savePriceTarget(priceTargetRequest)).thenReturn(priceTargetResponse);

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.priceTargetId", is("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
            .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.currentPrice", is("60000")))
            .andExpect(jsonPath("$.priceTarget", is("100000")))
            .andExpect(jsonPath("$.change", is(35.30)));
    }

    @Test
    void shouldSavePriceTargetWithMaxTargetWithStatus200() throws Exception {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("9999999999999999.999999999999"));
        var priceTargetResponse = new PriceTargetResponse(
            "2ca0a475-bf4b-4733-9f13-6be497ad6fe5",
            "Bitcoin",
            "60000",
            "9999999999999999.999999999999",
            35.30F
        );
        var payload = """
              {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": "9999999999999999.999999999999"
              }
            """;

        when(priceTargetServiceMock.savePriceTarget(priceTargetRequest)).thenReturn(priceTargetResponse);

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.priceTargetId", is("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
            .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.currentPrice", is("60000")))
            .andExpect(jsonPath("$.priceTarget", is("9999999999999999.999999999999")))
            .andExpect(jsonPath("$.change", is(35.30)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingPriceTargetWithBlankCryptoNameOrId() throws Exception {
        var payload = """
              {
                "cryptoNameOrId": " ",
                "priceTarget": "10000"
              }
            """;

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingPriceTargetWithEmptyCryptoNameOrId() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "",
                    "priceTarget": 100000
                }
            """;

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(
                jsonPath("$[*].detail")
                    .value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE, "Invalid crypto name"))
            );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingPriceTargetWithLongCryptoNameOrId() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName",
                    "priceTarget": 100000
                }
            """;

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_SIZE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {" bitcoin", "bitcoin ", "bit  coin"})
    void shouldFailWithStatus400WithOneMessageWhenSavingPriceTargetWithInvalidCryptoNameOrId(String cryptoNameOrId) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "%s",
                    "priceTarget": 100000
                }
            """.formatted(cryptoNameOrId);

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingPriceTargetWithNullCryptoNameOrId() throws Exception {
        var payload = """
                {
                    "priceTarget": 100000
                }
            """;

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingPriceTargetWithNullPriceTarget() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin"
                }
            """;

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Price target can not be null")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999.999999999999", "9999999999999999.9999999999999"})
    void shouldFailWithStatus400WithTwoMessagesWhenSavingPriceTargetWithInvalidPriceTarget(String target) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": %s
                }
            """.formatted(target);

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(
                jsonPath("$[*].detail")
                    .value(
                        containsInAnyOrder(
                            "Price target must be less than or equal to 9999999999999999.999999999999",
                            "Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                        )
                    )
            );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenSavingPriceTargetWithInvalidPriceTarget() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": "0.0000000000001"
                }
            """;

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(
                jsonPath(
                    "$[0].detail",
                    is("Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                )
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "-100", "0"})
    void shouldFailWithStatus400WithOneMessageWhenSavingPriceTargetWithNegativePriceTarget(String priceTarget) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": %s
                }
            """.formatted(priceTarget);

        mockMvc.perform(savePriceTarget(payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Price target must be greater than 0")));
    }

    @Test
    void shouldUpdatePriceTargetWithStatus200() throws Exception {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("100000"));
        var priceTargetResponse = getPriceTargetResponse();
        var payload = """
              {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": 100000
              }
            """;

        when(priceTargetServiceMock.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", priceTargetRequest))
            .thenReturn(priceTargetResponse);

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priceTargetId", is("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
            .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.currentPrice", is("60000")))
            .andExpect(jsonPath("$.priceTarget", is("100000")))
            .andExpect(jsonPath("$.change", is(35.30)));
    }

    @Test
    void shouldUpdatePriceTargetWithMaxPriceTargetWithStatus200() throws Exception {
        var priceTargetRequest = new PriceTargetRequest("bitcoin", new BigDecimal("9999999999999999.999999999999"));
        var priceTargetResponse = new PriceTargetResponse(
            "2ca0a475-bf4b-4733-9f13-6be497ad6fe5",
            "Bitcoin",
            "60000",
            "9999999999999999.999999999999",
            35.30F
        );
        var payload = """
              {
                "cryptoNameOrId": "bitcoin",
                "priceTarget": 9999999999999999.999999999999
              }
            """;

        when(priceTargetServiceMock.updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", priceTargetRequest))
            .thenReturn(priceTargetResponse);

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.priceTargetId", is("2ca0a475-bf4b-4733-9f13-6be497ad6fe5")))
            .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.currentPrice", is("60000")))
            .andExpect(jsonPath("$.priceTarget", is("9999999999999999.999999999999")))
            .andExpect(jsonPath("$.change", is(35.30)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingPriceTargetWithBlankCryptoNameOrId() throws Exception {
        var payload = """
              {
                "cryptoNameOrId": " ",
                "priceTarget": "10000"
              }
            """;

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingPriceTargetWithEmptyCryptoNameOrId() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "",
                    "priceTarget": 100000
                }
            """;

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(
                jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, CRYPTO_NAME_SIZE, "Invalid crypto name"))
            );
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingPriceTargetWithLongCryptoNameOrId() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "reallyLoooooooooooooooooooooooooooooooooooooooooooooooooooongName",
                    "priceTarget": 100000
                }
            """;

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_SIZE)));
    }

    @ParameterizedTest
    @ValueSource(strings = {" bitcoin", "bitcoin ", "bit  coin"})
    void shouldFailWithStatus400WithOneMessageWhenUpdatingPriceTargetWithInvalidCryptoNameOrId(String cryptoNameOrId) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "%s",
                    "priceTarget": 100000
                }
            """.formatted(cryptoNameOrId);

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingPriceTargetWithNullCryptoNameOrId() throws Exception {
        var payload = """
                {
                    "priceTarget": 100000
                }
            """;

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(CRYPTO_NAME_NOT_BLANK, "Invalid crypto name")));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingTargetWithNullPriceTarget() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin"
                }
            """;

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Price target can not be null")));
    }

    @ParameterizedTest
    @ValueSource(strings = {"99999999999999999.999999999999", "9999999999999999.9999999999999"})
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingPriceTargetWithInvalidPriceTarget(String priceTarget) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": %s
                }
            """.formatted(priceTarget);

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
            .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
            .andExpect(
                jsonPath("$[*].detail")
                    .value(
                        containsInAnyOrder(
                            "Price target must be less than or equal to 9999999999999999.999999999999",
                            "Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part"
                        )
                    )
            );
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenUpdatingPriceTargetWithInvalidPriceTarget() throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": "0.0000000000001"
                }
            """;

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(
                jsonPath(
                    "$[0].detail",
                    is("Price target must have up to 16 digits in the integer part and up to 12 digits in the decimal part")
                )
            );
    }

    @ParameterizedTest
    @ValueSource(strings = {"-5", "-100", "0"})
    void shouldFailWithStatus400WithOneMessageWhenUpdatingPriceTargetWithNegativePriceTarget(String priceTarget) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": %s
                }
            """.formatted(priceTarget);

        mockMvc.perform(updatePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5", payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Price target must be greater than 0")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
        "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    })
    void shouldFailWithStatus400WithOneMessageWhenUpdatingPriceTargetWithInvalidPriceTargetId(String priceTargetId) throws Exception {
        var payload = """
                {
                    "cryptoNameOrId": "bitcoin",
                    "priceTarget": "120000"
                }
            """;

        mockMvc.perform(updatePriceTarget(priceTargetId, payload))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is(INVALID_PRICE_TARGET_UUID)));
    }

    @Test
    void shouldDeletePriceTarget() throws Exception {
        mockMvc.perform(deletePriceTarget("2ca0a475-bf4b-4733-9f13-6be497ad6fe5"))
            .andExpect(status().isNoContent());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
        "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    })
    void shouldFailWithStatus400WithOneMessageWhenDeletingPriceTargetWithInvalidPriceTargetId(String priceTargetId) throws Exception {
        mockMvc.perform(deletePriceTarget(priceTargetId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is(INVALID_PRICE_TARGET_UUID)));
    }

    private PriceTargetResponse getPriceTargetResponse() {
        return new PriceTargetResponse(
            "2ca0a475-bf4b-4733-9f13-6be497ad6fe5",
            "Bitcoin",
            "60000",
            "100000",
            35.30F
        );
    }
}
