package com.distasilucas.cryptobalancetracker.controller;

import com.distasilucas.cryptobalancetracker.service.InsightsService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ExtendWith(SpringExtension.class)
@WebMvcTest(InsightsController.class)
public class InsightsControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private InsightsService insightsServiceMock;

    /*

    @Test
    fun `should retrieve total balances with status 200`() {
        every { insightsServiceMock.retrieveTotalBalancesInsights() } returns Optional.of(balances())

        mockMvc.retrieveTotalBalancesInsights()
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.totalUSDBalance", Matchers.`is`("100")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalBTCBalance", Matchers.`is`("0.1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalEURBalance", Matchers.`is`("70")))
    }

    @Test
    fun `should retrieve user cryptos insights for page with status 200`() {
        val page = 0
        val pageUserCryptosInsightsResponse = pageUserCryptosInsightsResponse(
                "676fb38a-556e-11ee-b56e-325096b39f47", listOf("BINANCE")
        )

        every {
            insightsServiceMock.retrieveUserCryptosInsights(page)
        } returns Optional.of(pageUserCryptosInsightsResponse)

        mockMvc.retrieveUserCryptosInsights(page)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.page", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hasNextPage", Matchers.`is`(false)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalUSDBalance", Matchers.`is`("4500.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalBTCBalance", Matchers.`is`("0.15")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalEURBalance", Matchers.`is`("4050.00")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].cryptoInfo.id",
                        Matchers.`is`("676fb38a-556e-11ee-b56e-325096b39f47")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoInfo.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].cryptoInfo.cryptoId",
                        Matchers.`is`("bitcoin")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoInfo.symbol", Matchers.`is`("btc")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].cryptoInfo.image",
                        Matchers.`is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].quantity", Matchers.`is`("0.15")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].percentage", Matchers.`is`(100.0)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalUSDBalance",
                        Matchers.`is`("4500.00")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalBTCBalance",
                        Matchers.`is`("0.15")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalEURBalance",
                        Matchers.`is`("4050.00")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].marketData.circulatingSupply",
                        Matchers.`is`("19000000")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].marketData.maxSupply", Matchers.`is`("21000000")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].marketData.currentPrice.usd",
                        Matchers.`is`("30000")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].marketData.currentPrice.eur",
                        Matchers.`is`("27000")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].marketData.currentPrice.btc", Matchers.`is`("1")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].platforms", Matchers.`is`(listOf("BINANCE"))))
    }

    @Test
    fun `should fail with status 400 with 1 message when retrieving user cryptos insights with invalid page`() {
        val page = -1

        mockMvc.retrieveUserCryptosInsights(page)
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$[0].detail",
                        Matchers.`is`("Page must be greater than or equal to 0")
                )
            )
    }

    @Test
    fun `should retrieve user cryptos platforms insights for page with status 200`() {
        val page = 0
        val pageUserCryptosInsightsResponse = pageUserCryptosInsightsResponse(platforms = listOf("BINANCE", "COINBASE"))

        every {
            insightsServiceMock.retrieveUserCryptosPlatformsInsights(page)
        } returns Optional.of(pageUserCryptosInsightsResponse)

        mockMvc.retrieveUserCryptosPlatformsInsights(page)
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.page", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.totalPages", Matchers.`is`(1)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.hasNextPage", Matchers.`is`(false)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalUSDBalance", Matchers.`is`("4500.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalBTCBalance", Matchers.`is`("0.15")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalEURBalance", Matchers.`is`("4050.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoInfo.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].cryptoInfo.cryptoId",
                        Matchers.`is`("bitcoin")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoInfo.symbol", Matchers.`is`("btc")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].cryptoInfo.image",
                        Matchers.`is`("https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].quantity", Matchers.`is`("0.15")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].percentage", Matchers.`is`(100.0)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalUSDBalance",
                        Matchers.`is`("4500.00")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalBTCBalance",
                        Matchers.`is`("0.15")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalEURBalance",
                        Matchers.`is`("4050.00")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].marketData.circulatingSupply",
                        Matchers.`is`("19000000")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].marketData.maxSupply", Matchers.`is`("21000000")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].marketData.currentPrice.usd",
                        Matchers.`is`("30000")
                )
            )
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].marketData.currentPrice.eur",
                        Matchers.`is`("27000")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].marketData.currentPrice.btc", Matchers.`is`("1")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].platforms",
                        Matchers.`is`(listOf("BINANCE", "COINBASE"))
                )
            )
    }

    @Test
    fun `should fail with status 400 with 1 message when retrieving user cryptos platforms insights with invalid page`() {
        val page = -1

        mockMvc.retrieveUserCryptosPlatformsInsights(page)
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$[0].detail",
                        Matchers.`is`("Page must be greater than or equal to 0")
                )
            )
    }

    @Test
    fun `should retrieve cryptos balances insights with status 200`() {
        val cryptosBalancesInsightsResponse = cryptosBalancesInsightsResponse()

        every {
            insightsServiceMock.retrieveCryptosBalancesInsights()
        } returns Optional.of(cryptosBalancesInsightsResponse)

        mockMvc.retrieveCryptosBalancesInsights()
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalUSDBalance", Matchers.`is`("7500.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalBTCBalance", Matchers.`is`("0.25")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalEURBalance", Matchers.`is`("6750.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoId", Matchers.`is`("bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].quantity", Matchers.`is`("0.25")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalUSDBalance",
                        Matchers.`is`("7500.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].balances.totalBTCBalance", Matchers.`is`("0.25")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalEURBalance",
                        Matchers.`is`("6750.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].percentage", Matchers.`is`(100.0)))

    }

    @Test
    fun `should retrieve platforms balances insights with status 200`() {
        val platformsBalancesInsightsResponse = platformsBalancesInsightsResponse()

        every {
            insightsServiceMock.retrievePlatformsBalancesInsights()
        } returns Optional.of(platformsBalancesInsightsResponse)

        mockMvc.retrievePlatformsBalancesInsights()
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalUSDBalance", Matchers.`is`("7500.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalBTCBalance", Matchers.`is`("0.25")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalEURBalance", Matchers.`is`("6750.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].platformName", Matchers.`is`("BINANCE")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.platforms[0].balances.totalUSDBalance",
                        Matchers.`is`("7500.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].balances.totalBTCBalance", Matchers.`is`("0.25")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.platforms[0].balances.totalEURBalance",
                        Matchers.`is`("6750.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].percentage", Matchers.`is`(100.0)))
    }

    @Test
    fun `should retrieve crypto insights with status 200`() {
        val cryptoInsightResponse = cryptoInsightResponse()

        every {
            insightsServiceMock.retrieveCryptoInsights("bitcoin")
        } returns Optional.of(cryptoInsightResponse)

        mockMvc.retrieveCryptoInsights("bitcoin")
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalUSDBalance", Matchers.`is`("4500.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalBTCBalance", Matchers.`is`("0.15")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalEURBalance", Matchers.`is`("4050.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].quantity", Matchers.`is`("0.15")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.platforms[0].balances.totalUSDBalance",
                        Matchers.`is`("4500.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].balances.totalBTCBalance", Matchers.`is`("0.15")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.platforms[0].balances.totalEURBalance",
                        Matchers.`is`("4050.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].percentage", Matchers.`is`(100.0)))
            .andExpect(MockMvcResultMatchers.jsonPath("$.platforms[0].platformName", Matchers.`is`("BINANCE")))
    }

    @Test
    fun `should retrieve platform insights with status 200`() {
        val platformInsightsResponse = platformInsightsResponse()

        every {
            insightsServiceMock.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
        } returns Optional.of(platformInsightsResponse)

        mockMvc.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111")
                .andExpect(MockMvcResultMatchers.status().isOk)
                .andExpect(MockMvcResultMatchers.jsonPath("$.platformName", Matchers.`is`("BINANCE")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalUSDBalance", Matchers.`is`("4500.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalBTCBalance", Matchers.`is`("0.15")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.balances.totalEURBalance", Matchers.`is`("4050.00")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoName", Matchers.`is`("Bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].cryptoId", Matchers.`is`("bitcoin")))
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].quantity", Matchers.`is`("0.15")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalUSDBalance",
                        Matchers.`is`("4500.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].balances.totalBTCBalance", Matchers.`is`("0.15")))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$.cryptos[0].balances.totalEURBalance",
                        Matchers.`is`("4050.00")
                )
            )
            .andExpect(MockMvcResultMatchers.jsonPath("$.cryptos[0].percentage", Matchers.`is`(100.0)))
    }

    @ParameterizedTest
    @ValueSource(
            strings = [
            "123e4567-e89b-12d3-a456-4266141740001", "123e4567-e89b-12d3-a456-42661417400",
            "123e456-e89b-12d3-a456-426614174000", "123e45676-e89b-12d3-a456-426614174000"
            ]
    )
    fun `should fail with status 400 with 1 message when retrieving platform insights with invalid id`(platformId: String) {
        mockMvc.retrievePlatformInsights(platformId)
                .andExpect(MockMvcResultMatchers.status().isBadRequest)
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$", Matchers.hasSize<Int>(1)))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].title", Matchers.`is`("Bad Request")))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status", Matchers.`is`(400)))
            .andExpect(
                MockMvcResultMatchers.jsonPath(
                        "$[0].detail",
                        Matchers.`is`(PLATFORM_ID_UUID)
                )
            )
    }

    private fun pageUserCryptosInsightsResponse(
            id: String? = null,
            platforms: List<String>
    ) = PageUserCryptosInsightsResponse(
            page = 0,
            totalPages = 1,
            balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
    ),
    cryptos = listOf(
            UserCryptosInsights(
            cryptoInfo = CryptoInfo(
            id = id,
            cryptoName = "Bitcoin",
            coingeckoCryptoId = "bitcoin",
            symbol = "btc",
            image = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
    ),
    quantity = "0.15",
    percentage = 100f,
    balances = BalancesResponse(
            totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
    ),
    marketData = MarketData(
            circulatingSupply = "19000000",
            maxSupply = "21000000",
            currentPrice = CurrentPrice(
                    usd = "30000",
            eur = "27000",
            btc = "1"
    )
                ),
    platforms = platforms
            )
                    )
                    )

    private fun cryptosBalancesInsightsResponse() = CryptosBalancesInsightsResponse(
            balances = BalancesResponse(
            totalUSDBalance = "7500.00",
            totalBTCBalance = "0.25",
            totalEURBalance = "6750.00"
    ),
    cryptos = listOf(
            CryptoInsights(
            cryptoName = "Bitcoin",
            cryptoId = "bitcoin",
            quantity = "0.25",
            balances = BalancesResponse(
                    totalUSDBalance = "7500.00",
            totalBTCBalance = "0.25",
            totalEURBalance = "6750.00"
    ),
    percentage = 100f
            )
            )
            )

    private fun platformsBalancesInsightsResponse() = PlatformsBalancesInsightsResponse(
            balances = BalancesResponse(
            totalUSDBalance = "7500.00",
            totalBTCBalance = "0.25",
            totalEURBalance = "6750.00"
    ),
    platforms = listOf(
            PlatformsInsights(
            platformName = "BINANCE",
            balances = BalancesResponse(
                    totalUSDBalance = "7500.00",
            totalBTCBalance = "0.25",
            totalEURBalance = "6750.00"
    ),
    percentage = 100f
            )
            )
            )

    private fun cryptoInsightResponse() = CryptoInsightResponse(
            cryptoName = "Bitcoin",
            balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
    ),
    platforms = listOf(
            PlatformInsight(
            quantity = "0.15",
            balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
    ),
    percentage = 100f,
    platformName = "BINANCE"
            )
            )
            )

    private fun platformInsightsResponse() = PlatformInsightsResponse(
            platformName = "BINANCE",
            balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
    ),
    cryptos = listOf(
            CryptoInsights(
            cryptoName = "Bitcoin",
            cryptoId = "bitcoin",
            quantity = "0.15",
            balances = BalancesResponse(
                    totalUSDBalance = "4500.00",
            totalBTCBalance = "0.15",
            totalEURBalance = "4050.00"
    ),
    percentage = 100f
            )
            )
            )

            */
}
