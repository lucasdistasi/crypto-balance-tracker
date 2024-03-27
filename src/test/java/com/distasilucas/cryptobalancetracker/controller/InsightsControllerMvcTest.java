package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.SortBy;
import com.distasilucas.cryptobalancetracker.model.SortParams;
import com.distasilucas.cryptobalancetracker.model.SortType;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CirculatingSupply;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.CurrentPrice;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalances;
import com.distasilucas.cryptobalancetracker.model.response.insights.MarketData;
import com.distasilucas.cryptobalancetracker.model.response.insights.PriceChange;
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights;
import com.distasilucas.cryptobalancetracker.service.InsightsService;
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
import java.util.Optional;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBalances;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveCryptoInsights;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveCryptosBalancesInsights;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveDatesBalances;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrievePlatformInsights;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrievePlatformsBalancesInsights;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveTotalBalancesInsights;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveUserCryptosInsights;
import static com.distasilucas.cryptobalancetracker.TestDataSource.retrieveUserCryptosPlatformsInsights;
import static com.distasilucas.cryptobalancetracker.constants.ValidationConstants.PLATFORM_ID_UUID;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(SpringExtension.class)
@WebMvcTest(InsightsController.class)
class InsightsControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsightsService insightsServiceMock;

    private static final SortParams sortParams = new SortParams(SortBy.PERCENTAGE, SortType.DESC);

    @Test
    void shouldRetrieveTotalBalancesWithStatus200() throws Exception {
        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(Optional.of(getBalances()));

        mockMvc.perform(retrieveTotalBalancesInsights())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUSDBalance", is("100")))
            .andExpect(jsonPath("$.totalBTCBalance", is("0.1")))
            .andExpect(jsonPath("$.totalEURBalance", is("70")));
    }

    @Test
    void shouldRetrieveZeroTotalBalancesWithStatus200() throws Exception {
        when(insightsServiceMock.retrieveTotalBalancesInsights()).thenReturn(Optional.empty());

        mockMvc.perform(retrieveTotalBalancesInsights())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalUSDBalance", is("0")))
            .andExpect(jsonPath("$.totalBTCBalance", is("0")))
            .andExpect(jsonPath("$.totalEURBalance", is("0")));
    }

    @Test
    void shouldRetrieveDatesBalancesResponseWithStatus200() throws Exception {
        var datesBalances = new DatesBalances("22 February 2024", "1000");
        var datesBalanceResponse = new DatesBalanceResponse(List.of(datesBalances), 5, "0");

        when(insightsServiceMock.retrieveDatesBalances(DateRange.ONE_WEEK))
            .thenReturn(Optional.of(datesBalanceResponse));

        mockMvc.perform(retrieveDatesBalances())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.datesBalances[0].date", is("22 February 2024")))
            .andExpect(jsonPath("$.datesBalances[0].balance", is("1000")))
            .andExpect(jsonPath("$.change", is(5.0)));
    }

    @Test
    void shouldRetrieveUserCryptosInsightsForPageWithStatus200() throws Exception {
        var page = 0;
        var pageUserCryptosInsightsResponse = getPageUserCryptosInsightsResponse(
            "676fb38a-556e-11ee-b56e-325096b39f47", List.of("BINANCE")
        );

        when(insightsServiceMock.retrieveUserCryptosInsights(page, sortParams))
            .thenReturn(Optional.of(pageUserCryptosInsightsResponse));

        mockMvc.perform(retrieveUserCryptosInsights(page))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.hasNextPage", is(false)))
            .andExpect(jsonPath("$.balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.id", is("676fb38a-556e-11ee-b56e-325096b39f47")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoId", is("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.symbol", is("btc")))
            .andExpect(
                jsonPath(
                    "$.cryptos[0].cryptoInfo.image",
                    is("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
                )
            )
            .andExpect(jsonPath("$.cryptos[0].quantity", is("0.15")))
            .andExpect(jsonPath("$.cryptos[0].percentage", is(100.0)))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].marketCapRank", is(1)))
            .andExpect(jsonPath("$.cryptos[0].marketData.circulatingSupply.totalCirculatingSupply", is("19000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.circulatingSupply.percentage", is(90.48)))
            .andExpect(jsonPath("$.cryptos[0].marketData.maxSupply", is("21000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.usd", is("30000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.eur", is("27000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.btc", is("1")))
            .andExpect(jsonPath("$.cryptos[0].marketData.marketCap", is("813208997089")))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn24h", is(10.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn7d", is(-5.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn30d", is(0.00)))
            .andExpect(jsonPath("$.cryptos[0].platforms", is(List.of("BINANCE"))));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenRetrievingUserCryptosInsightsWithInvalidPage() throws Exception {
        var page = -1;

        mockMvc.perform(retrieveUserCryptosInsights(page))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is("Page must be greater than or equal to 0")));
    }

    @Test
    void shouldRetrieveUserCryptosPlatformsInsightsForPageWithStatus200() throws Exception {
        var page = 0;
        var pageUserCryptosInsightsResponse = getPageUserCryptosInsightsResponse(
            null,
            List.of("BINANCE", "COINBASE")
        );

        when(insightsServiceMock.retrieveUserCryptosPlatformsInsights(page, sortParams))
            .thenReturn(Optional.of(pageUserCryptosInsightsResponse));

        mockMvc.perform(retrieveUserCryptosPlatformsInsights(page))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.page", is(1)))
            .andExpect(jsonPath("$.totalPages", is(1)))
            .andExpect(jsonPath("$.hasNextPage", is(false)))
            .andExpect(jsonPath("$.balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.cryptoId", is("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoInfo.symbol", is("btc")))
            .andExpect(
                jsonPath(
                    "$.cryptos[0].cryptoInfo.image",
                    is("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
                )
            )
            .andExpect(jsonPath("$.cryptos[0].quantity", is("0.15")))
            .andExpect(jsonPath("$.cryptos[0].percentage", is(100.0)))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].marketCapRank", is(1)))
            .andExpect(jsonPath("$.cryptos[0].marketData.circulatingSupply.totalCirculatingSupply", is("19000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.circulatingSupply.percentage", is(90.48)))
            .andExpect(jsonPath("$.cryptos[0].marketData.maxSupply", is("21000000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.usd", is("30000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.eur", is("27000")))
            .andExpect(jsonPath("$.cryptos[0].marketData.currentPrice.btc", is("1")))
            .andExpect(jsonPath("$.cryptos[0].marketData.marketCap", is("813208997089")))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn24h", is(10.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn7d", is(-5.00)))
            .andExpect(jsonPath("$.cryptos[0].marketData.priceChange.changePercentageIn30d", is(0.00)))
            .andExpect(jsonPath("$.cryptos[0].platforms", is(List.of("BINANCE", "COINBASE"))));
    }

    @Test
    void shouldFailWithStatus400WithOneMessageWhenRetrievingUserCryptosPlatformsInsightsWithInvalidPage() throws Exception {
        var page = -1;

        mockMvc.perform(retrieveUserCryptosPlatformsInsights(page))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(
                jsonPath(
                    "$[0].detail",
                    is("Page must be greater than or equal to 0")
                )
            );
    }

    @Test
    void shouldRetrieveCryptosBalancesInsightsWithStatus200() throws Exception {
        var cryptosBalancesInsightsResponse = getCryptosBalancesInsightsResponse();

        when(insightsServiceMock.retrieveCryptosBalancesInsights()).thenReturn(Optional.of(cryptosBalancesInsightsResponse));

        mockMvc.perform(retrieveCryptosBalancesInsights())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balances.totalUSDBalance", is("7500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", is("0.25")))
            .andExpect(jsonPath("$.balances.totalEURBalance", is("6750.00")))
            .andExpect(jsonPath("$.cryptos[0].cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoId", is("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].quantity", is("0.25")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", is("7500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", is("0.25")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", is("6750.00")))
            .andExpect(jsonPath("$.cryptos[0].percentage", is(100.0)));
    }

    @Test
    void shouldRetrievePlatformsBalancesInsightsWithStatus200() throws Exception {
        var platformsBalancesInsightsResponse = getPlatformsBalancesInsightsResponse();

        when(insightsServiceMock.retrievePlatformsBalancesInsights())
            .thenReturn(Optional.of(platformsBalancesInsightsResponse));

        mockMvc.perform(retrievePlatformsBalancesInsights())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.balances.totalUSDBalance", is("7500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", is("0.25")))
            .andExpect(jsonPath("$.balances.totalEURBalance", is("6750.00")))
            .andExpect(jsonPath("$.platforms[0].platformName", is("BINANCE")))
            .andExpect(jsonPath("$.platforms[0].balances.totalUSDBalance", is("7500.00")))
            .andExpect(jsonPath("$.platforms[0].balances.totalBTCBalance", is("0.25")))
            .andExpect(jsonPath("$.platforms[0].balances.totalEURBalance", is("6750.00")))
            .andExpect(jsonPath("$.platforms[0].percentage", is(100.0)));
    }

    @Test
    void shouldRetrieveCryptoInsightsWithStatus200() throws Exception {
        var cryptoInsightResponse = getCryptoInsightResponse();

        when(insightsServiceMock.retrieveCryptoInsights("bitcoin")).thenReturn(Optional.of(cryptoInsightResponse));

        mockMvc.perform(retrieveCryptoInsights("bitcoin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.platforms[0].quantity", is("0.15")))
            .andExpect(jsonPath("$.platforms[0].balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.platforms[0].balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.platforms[0].balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.platforms[0].percentage", is(100.0)))
            .andExpect(jsonPath("$.platforms[0].platformName", is("BINANCE")));
    }

    @Test
    void shouldRetrievePlatformInsightsWithStatus200() throws Exception {
        var platformInsightsResponse = getPlatformInsightsResponse();

        when(insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111"))
            .thenReturn(Optional.of(platformInsightsResponse));

        mockMvc.perform(retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.platformName", is("BINANCE")))
            .andExpect(jsonPath("$.balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].id", is("1f832f95-62e3-4d1b-a1e6-982d8c22f2bb")))
            .andExpect(jsonPath("$.cryptos[0].cryptoName", is("Bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].cryptoId", is("bitcoin")))
            .andExpect(jsonPath("$.cryptos[0].quantity", is("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalUSDBalance", is("4500.00")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalBTCBalance", is("0.15")))
            .andExpect(jsonPath("$.cryptos[0].balances.totalEURBalance", is("4050.00")))
            .andExpect(jsonPath("$.cryptos[0].percentage", is(100.0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
        "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
    })
    void shouldFailWithStatus400WithOneMessageWhenRetrievingPlatformInsightsWithInvalidId(String platformId) throws Exception {
        mockMvc.perform(retrievePlatformInsights(platformId))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title", is("Bad Request")))
            .andExpect(jsonPath("$[0].status", is(400)))
            .andExpect(jsonPath("$[0].detail", is(PLATFORM_ID_UUID)));
    }

    public PageUserCryptosInsightsResponse getPageUserCryptosInsightsResponse(String id, List<String> platforms) {
        return new PageUserCryptosInsightsResponse(
            0,
            1,
            new BalancesResponse("4500.00", "4050.00", "0.15"),
            List.of(
                new UserCryptosInsights(
                    new CryptoInfo(
                        id,
                        "Bitcoin",
                        "bitcoin",
                        "btc",
                        "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                    ),
                    "0.15",
                    100f,
                    new BalancesResponse("4500.00", "4050.00", "0.15"),
                    1,
                    new MarketData(
                        new CirculatingSupply("19000000", 90.48f),
                        "21000000",
                        new CurrentPrice("30000", "27000", "1"),
                        "813208997089",
                        new PriceChange(
                            new BigDecimal("10.00"),
                            new BigDecimal("-5.00"),
                            new BigDecimal("0.00")
                        )
                    ),
                    platforms
                )
            )
        );
    }

    private CryptosBalancesInsightsResponse getCryptosBalancesInsightsResponse() {
        return new CryptosBalancesInsightsResponse(
            new BalancesResponse("7500.00", "6750.00", "0.25"),
            List.of(
                new CryptoInsights(
                    "1f832f95-62e3-4d1b-a1e6-982d8c22f2bb",
                    "Bitcoin",
                    "bitcoin",
                    "0.25",
                    new BalancesResponse("7500.00", "6750.00", "0.25"),
                    100f
                )
            )
        );
    }

    private PlatformsBalancesInsightsResponse getPlatformsBalancesInsightsResponse() {
        return new PlatformsBalancesInsightsResponse(
            new BalancesResponse("7500.00", "6750.00", "0.25"),
            List.of(
                new PlatformsInsights(
                    "BINANCE",
                    new BalancesResponse("7500.00", "6750.00", "0.25"),
                    100f
                )
            )
        );
    }

    private CryptoInsightResponse getCryptoInsightResponse() {
        return new CryptoInsightResponse(
            "Bitcoin",
            new BalancesResponse("4500.00", "4050.00", "0.15"),
            List.of(
                new PlatformInsight(
                    "0.15",
                    new BalancesResponse("4500.00", "4050.00", "0.15"),
                    100f,
                    "BINANCE"
                )
            )
        );
    }

    private PlatformInsightsResponse getPlatformInsightsResponse() {
        return new PlatformInsightsResponse(
            "BINANCE",
            new BalancesResponse("4500.00", "4050.00", "0.15"),
            List.of(
                new CryptoInsights(
                    "1f832f95-62e3-4d1b-a1e6-982d8c22f2bb",
                    "Bitcoin",
                    "bitcoin",
                    "0.15",
                    new BalancesResponse("4500.00", "4050.00", "0.15"),
                    100f
                )
            )
        );
    }
}
