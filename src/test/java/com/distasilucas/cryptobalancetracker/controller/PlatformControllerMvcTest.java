package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.model.request.platform.PlatformRequest;
import com.distasilucas.cryptobalancetracker.service.PlatformService;
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

import java.util.Collections;
import java.util.List;

import static com.distasilucas.cryptobalancetracker.TestDataSource.deletePlatform;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getFileContent;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveAllPlatforms;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrievePlatformById;
import static com.distasilucas.cryptobalancetracker.TestDataSource.savePlatform;
import static com.distasilucas.cryptobalancetracker.TestDataSource.updatePlatform;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.INVALID_PLATFORM_NAME;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.NULL_BLANK_PLATFORM_NAME;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;
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
@WebMvcTest(PlatformController.class)
class PlatformControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PlatformService platformServiceMock;

    @Test
    void shouldRetrieveAllPlatformsWithStatus200() throws Exception {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(platformServiceMock.retrieveAllPlatforms()).thenReturn(List.of(platformEntity));

        mockMvc.perform(retrieveAllPlatforms())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")))
                .andExpect(jsonPath("$[0].name", is("BINANCE")));
    }

    @Test
    void shouldRetrieveAllPlatformsWithStatus204() throws Exception {
        when(platformServiceMock.retrieveAllPlatforms()).thenReturn(Collections.emptyList());

        mockMvc.perform(retrieveAllPlatforms())
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldRetrievePlatformByIdWithStatus200() throws Exception {
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");

        when(platformServiceMock.retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
                .thenReturn(platformEntity);

        mockMvc.perform(retrievePlatformById("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")))
                .andExpect(jsonPath("$.name", is("BINANCE")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "4f663841-7c82-4d0f-a756-cf7d4e2d3bc", "4f66381-7c82-4d0f-a756-cf7d4e2d3bc6", "4f663841-7c82-4d0f-a56-cf7d4e2d3bc6",
            "4f663841-7c82-4d0f-a756cf7d4e2d3bc6", "4f6638417c82-4d0f-a756-cf7d4e2d3bc6", "4f663841-7c82-4d0fa756-cf7d4e2d3bc6"
    })
    void shouldFailWithStatus400WithOneMessageWhenRetrievingPlatformWithInvalidId(String platformId) throws Exception {
        mockMvc.perform(retrievePlatformById(platformId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(PLATFORM_ID_UUID)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"binance", "OKX", "Coinbase", "Kraken", "Gate Io"})
    void shouldSavePlatformWithStatus200(String platformName) throws Exception {
        var platformRequest = new PlatformRequest(platformName);
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformName.toUpperCase());
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted(platformName);

        when(platformServiceMock.savePlatform(platformRequest)).thenReturn(platformEntity);

        mockMvc.perform(savePlatform(content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")))
                .andExpect(jsonPath("$.name", is(platformName.toUpperCase())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "C01nb453", "NmkwRsgZuYqEPvDbAtIoCfLHX", "Coinba#e", "Gate  Io", " Gate Io", "Gate Io "})
    void shouldFailWithStatus400WithOneMessageWhenSavingInvalidPlatform(String platformName) throws Exception {
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted(platformName);

        mockMvc.perform(savePlatform(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(INVALID_PLATFORM_NAME)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", ""})
    void shouldFailWithStatus400WithTwoMessagesWhenSavingBlankOrEmptyPlatform(String platformName) throws Exception {
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted(platformName);

        mockMvc.perform(savePlatform(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(NULL_BLANK_PLATFORM_NAME, INVALID_PLATFORM_NAME)));
    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenSavingNullPlatform() throws Exception {
        var content = """
                    {
                        "name": null
                    }
                """;

        mockMvc.perform(savePlatform(content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(NULL_BLANK_PLATFORM_NAME, INVALID_PLATFORM_NAME)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"binance", "OKX", "Coinbase", "Kraken", "Coinbase Exchange", "Gate IO"})
    void shouldUpdatePlatformWithStatus200(String platformName) throws Exception {
        var platformRequest = new PlatformRequest(platformName);
        var platformEntity = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformName.toUpperCase());
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted(platformName);

        when(platformServiceMock.updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", platformRequest))
                .thenReturn(platformEntity);

        mockMvc.perform(updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", content))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6")))
                .andExpect(jsonPath("$.name", is(platformName.toUpperCase())));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    })
    void shouldFailWithStatus400WithOneMessageWhenUpdatingPlatformWithInvalidId(String platformId) throws Exception {
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted("bybit");

        mockMvc.perform(updatePlatform(platformId, content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(PLATFORM_ID_UUID)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123", "C01nb453", "NmkwRsgZuYqEPvDbAtIoCfLHX", "Coinba#e", "Gate  Io", " Gate Io", "Gate Io "})
    void shouldFailWithStatus400WithOneMessagesWhenUpdatingInvalidPlatformName(String platformName) throws Exception {
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted(platformName);

        mockMvc.perform(updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(INVALID_PLATFORM_NAME)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ", ""})
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingBlankOrEmptyPlatform(String platformName) throws Exception {
        var content = getFileContent("/request/platform/save_update_platform_request.json")
                .formatted(platformName);

        mockMvc.perform(updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(NULL_BLANK_PLATFORM_NAME, INVALID_PLATFORM_NAME)));

    }

    @Test
    void shouldFailWithStatus400WithTwoMessagesWhenUpdatingNullPlatform() throws Exception {
        var content = """
                    {
                        "name": null
                    }
                """;

        mockMvc.perform(updatePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", content))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title").value(everyItem(is("Bad Request"))))
                .andExpect(jsonPath("$[*].status").value(everyItem(is(400))))
                .andExpect(jsonPath("$[*].detail").value(containsInAnyOrder(NULL_BLANK_PLATFORM_NAME, INVALID_PLATFORM_NAME)));

    }

    @Test
    void shouldDeletePlatformWithStatus200() throws Exception {
        doNothing().when(platformServiceMock).deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6");

        mockMvc.perform(deletePlatform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))
                .andExpect(status().isOk());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    })
    void shouldFailWithStatus400WithOneMessageWhenDeletingInvalidPlatform(String platformId) throws Exception {
        mockMvc.perform(deletePlatform(platformId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("Bad Request")))
                .andExpect(jsonPath("$[0].status", is(400)))
                .andExpect(jsonPath("$[0].detail", is(PLATFORM_ID_UUID)));
    }

}
