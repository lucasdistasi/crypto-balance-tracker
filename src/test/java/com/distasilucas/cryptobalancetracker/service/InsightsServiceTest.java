package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.CurrentPrice;
import com.distasilucas.cryptobalancetracker.model.response.insights.MarketData;
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getCryptoEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getPlatformEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getUserCrypto;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

class InsightsServiceTest {

    @Mock
    private PlatformService platformServiceMock;

    @Mock
    private UserCryptoService userCryptoServiceMock;

    @Mock
    private CryptoService cryptoServiceMock;

    private InsightsService insightsService;

    @BeforeEach
    void setUp() {
        openMocks(this);
        insightsService = new InsightsService(platformServiceMock, userCryptoServiceMock, cryptoServiceMock);
    }

    @Test
    void shouldRetrieveTotalBalancesInsights() {
        var cryptos = List.of("bitcoin", "tether", "ethereum", "litecoin");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();

        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);
        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum", "litecoin"))).thenReturn(cryptosEntities);

        var balances = insightsService.retrieveTotalBalancesInsights();

        assertThat(balances)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(new BalancesResponse("7108.39", "6484.23", "0.25127935932")));
    }

    @Test
    void shouldRetrieveEmptyForTotalBalancesInsights() {
        when(userCryptoServiceMock.findAll()).thenReturn(emptyList());

        var balances = insightsService.retrieveTotalBalancesInsights();

        assertThat(balances)
                .usingRecursiveComparison()
                .isEqualTo(Optional.empty());
    }

    @Test
    void shouldRetrievePlatformInsightsWithOneCrypto() {
        var platformEntity = getPlatformEntity();
        var userCryptos = getUserCrypto();
        var bitcoinCryptoEntity = getCryptoEntity();

        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")).thenReturn(platformEntity);
        when(userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")).thenReturn(List.of(userCryptos));
        when(cryptoServiceMock.findAllByIds(List.of("bitcoin"))).thenReturn(List.of(bitcoinCryptoEntity));

        var platformInsightsResponse = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        var expected = new PlatformInsightsResponse(
                "BINANCE",
                new BalancesResponse("7500.00", "6750.00", "0.25"),
                List.of(new CryptoInsights("Bitcoin", "bitcoin", "0.25", new BalancesResponse("7500.00", "6750.00", "0.25"), 100f))
        );
        assertThat(platformInsightsResponse)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrievePlatformInsightsWithMultipleCryptos() {
        var localDateTime = LocalDateTime.now();
        var platformEntity = getPlatformEntity();
        var bitcoinUserCrypto = getUserCrypto();
        var polkadotUserCrypto = new UserCrypto("polkadot", new BigDecimal("100"), "123e4567-e89b-12d3-a456-426614174111");
        var bitcoinCryptoEntity = getCryptoEntity();
        var polkadotCryptoEntity = new Crypto(
                "polkadot",
                "Polkadot",
                "dot",
                "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
                new BigDecimal("4.25"),
                new BigDecimal("3.97"),
                new BigDecimal("0.00016554"),
                new BigDecimal("1272427996.25919"),
                BigDecimal.ZERO,
                localDateTime
        );

        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")).thenReturn(platformEntity);
        when(userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111"))
                .thenReturn(List.of(bitcoinUserCrypto, polkadotUserCrypto));
        when(cryptoServiceMock.findAllByIds(List.of("bitcoin", "polkadot")))
                .thenReturn(List.of(bitcoinCryptoEntity, polkadotCryptoEntity));

        var platformInsightsResponse = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        var expected = new PlatformInsightsResponse(
                "BINANCE",
                new BalancesResponse("7925.00", "7147.00", "0.266554"),
                List.of(
                        new CryptoInsights(
                                "Bitcoin",
                                "bitcoin",
                                "0.25",
                                new BalancesResponse("7500.00", "6750.00", "0.25"),
                                94.64f
                        ),
                        new CryptoInsights(
                                "Polkadot",
                                "polkadot",
                                "100",
                                new BalancesResponse("425.00", "397.00", "0.016554"),
                                5.36f
                        )
                )
        );
        assertThat(platformInsightsResponse)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrieveEmptyIfNoCryptosAreFoundForRetrievePlatformInsights() {
        when(userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")).thenReturn(emptyList());

        var platformInsights = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        assertTrue(platformInsights.isEmpty());
    }

    @Test
    void shouldRetrieveCoingeckoCryptoIdInsightsWithOnePlatform() {
        var bitcoinUserCrypto = getUserCrypto();
        var binancePlatform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var bitcoinCryptoEntity = getCryptoEntity();

        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(List.of(bitcoinUserCrypto));
        when(platformServiceMock.findAllByIds(List.of("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6"))).thenReturn(List.of(binancePlatform));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(bitcoinCryptoEntity);

        var cryptoInsightsResponse = insightsService.retrieveCryptoInsights("bitcoin");

        var expected = new CryptoInsightResponse(
                "Bitcoin",
                new BalancesResponse("7500.00", "6750.00", "0.25"),
                List.of(
                        new PlatformInsight(
                                "0.25",
                                new BalancesResponse("7500.00", "6750.00", "0.25"),
                                100f,
                                "BINANCE"
                        )
                )
        );
        assertThat(cryptoInsightsResponse)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrieveCoingeckoCryptoIdInsightsWithMultiplePlatforms() {
        var bitcoinUserCrypto = List.of(
                getUserCrypto(),
                new UserCrypto(
                        "ed34425b-d9f7-4244-bd16-0212621848c6",
                        "bitcoin",
                        new BigDecimal("0.03455"),
                        "fa3db02d-4d43-416a-951b-e7ea3a4fe386"
                )
        );
        var binancePlatform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var coinbasePlatform = new Platform("fa3db02d-4d43-416a-951b-e7ea3a4fe386", "COINBASE");
        var bitcoinCryptoEntity = getCryptoEntity();

        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(bitcoinUserCrypto);
        when(platformServiceMock.findAllByIds(
                List.of("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "fa3db02d-4d43-416a-951b-e7ea3a4fe386")
        )).thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(cryptoServiceMock.retrieveCryptoInfoById("bitcoin")).thenReturn(bitcoinCryptoEntity);

        var cryptoInsightResponse = insightsService.retrieveCryptoInsights("bitcoin");

        var expected = new CryptoInsightResponse(
                "Bitcoin",
                new BalancesResponse("8536.50", "7682.85", "0.28455"),
                List.of(
                        new PlatformInsight(
                                "0.25",
                                new BalancesResponse("7500.00", "6750.00", "0.25"),
                                87.86f,
                                "BINANCE"
                        ),
                        new PlatformInsight(
                                "0.03455",
                                new BalancesResponse("1036.50", "932.85", "0.03455"),
                                12.14f,
                                "COINBASE"
                        )
                )
        );
        assertThat(cryptoInsightResponse)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrieveEmptyIfNoCryptosAreFoundForRetrieveCryptoInsights() {
        when(userCryptoServiceMock.findAllByCoingeckoCryptoId("bitcoin")).thenReturn(emptyList());

        var cryptoInsightResponse = insightsService.retrieveCryptoInsights("bitcoin");

        assertTrue(cryptoInsightResponse.isEmpty());
    }

    @Test
    void shouldRetrievePlatformsBalancesInsights() {
        var cryptos = List.of("bitcoin", "tether", "ethereum", "litecoin");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum", "litecoin"))).thenReturn(cryptosEntities);

        var platformBalancesInsightsResponse = insightsService.retrievePlatformsBalancesInsights();

        var expected = new PlatformsBalancesInsightsResponse(
                new BalancesResponse("7108.39", "6484.23", "0.25127935932"),
                List.of(
                        new PlatformsInsights(
                                "BINANCE",
                                new BalancesResponse("5120.45", "4629.06", "0.1740889256"),
                                72.03f
                        ),
                        new PlatformsInsights(
                                "COINBASE",
                                new BalancesResponse("1987.93", "1855.17", "0.07719043372"),
                                27.97f
                        )
                )
        );
        assertThat(platformBalancesInsightsResponse)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrieveEmptyIfNoCryptosAreFoundForRetrievePlatformBalancesInsights() {
        when(userCryptoServiceMock.findAll()).thenReturn(emptyList());

        var platformBalancesInsightsResponse = insightsService.retrievePlatformsBalancesInsights();

        assertTrue(platformBalancesInsightsResponse.isEmpty());
    }

    @Test
    void shouldRetrieveCryptosBalancesInsights() {
        var cryptos = List.of("bitcoin", "tether", "ethereum", "litecoin");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();

        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);
        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum", "litecoin"))).thenReturn(cryptosEntities);

        var cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights();

        var expected = new CryptosBalancesInsightsResponse(
                new BalancesResponse("7108.39", "6484.23", "0.25127935932"),
                List.of(
                        new CryptoInsights(
                                "Bitcoin",
                                "bitcoin",
                                "0.15",
                                new BalancesResponse("4500.00", "4050.00", "0.15"),
                                63.31f
                        ),
                        new CryptoInsights(
                                "Ethereum",
                                "ethereum",
                                "1.372",
                                new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                31.22f
                        ),
                        new CryptoInsights(
                                "Tether",
                                "tether",
                                "200",
                                new BalancesResponse("199.92", "186.62", "0.00776"),
                                2.81f
                        ),
                        new CryptoInsights(
                                "Litecoin",
                                "litecoin",
                                "3.125",
                                new BalancesResponse("189.34", "176.75", "0.007352875"),
                                2.66f
                        )
                )
        );
        assertThat(cryptosBalancesInsightsResponse)
                .usingRecursiveComparison()
                .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrieveCryptosBalancesInsightsWithOthers() {
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos());
        when(cryptoServiceMock.findAllByIds(
                Set.of(
                        "bitcoin",
                        "tether",
                        "ethereum",
                        "litecoin",
                        "binancecoin",
                        "ripple",
                        "cardano",
                        "polkadot",
                        "solana",
                        "matic-network",
                        "chainlink",
                        "dogecoin",
                        "avalanche-2",
                        "uniswap"
                )
        )).thenReturn(cryptos());

        var cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights();

        assertThat(cryptosBalancesInsightsResponse)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                new CryptosBalancesInsightsResponse(
                                        new BalancesResponse("8373.63", "7663.61", "0.29959591932"),
                                        List.of(
                                                new CryptoInsights(
                                                        "Bitcoin",
                                                        "bitcoin",
                                                        "0.15",
                                                        new BalancesResponse("4500.00", "4050.00", "0.15"),
                                                        53.74f
                                                ),
                                                new CryptoInsights(
                                                        "Ethereum",
                                                        "ethereum",
                                                        "1.372",
                                                        new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                                        26.5f
                                                ),
                                                new CryptoInsights(
                                                        "Avalanche",
                                                        "avalanche-2",
                                                        "25",
                                                        new BalancesResponse("232.50", "216.75", "0.008879"),
                                                        2.78f
                                                ),
                                                new CryptoInsights(
                                                        "BNB",
                                                        "binancecoin",
                                                        "1",
                                                        new BalancesResponse("211.79", "197.80", "0.00811016"),
                                                        2.53f
                                                ),
                                                new CryptoInsights(
                                                        "Chainlink",
                                                        "chainlink",
                                                        "35",
                                                        new BalancesResponse("209.65", "195.30", "0.0080031"),
                                                        2.5f
                                                ),
                                                new CryptoInsights(
                                                        "Tether",
                                                        "tether",
                                                        "200",
                                                        new BalancesResponse("199.92", "186.62", "0.00776"),
                                                        2.39f
                                                ),
                                                new CryptoInsights(
                                                        "Litecoin",
                                                        "litecoin",
                                                        "3.125",
                                                        new BalancesResponse("189.34", "176.75", "0.007352875"),
                                                        2.26f
                                                ),
                                                new CryptoInsights(
                                                        "Solana",
                                                        "solana",
                                                        "10",
                                                        new BalancesResponse("180.40", "168.20", "0.0068809"),
                                                        2.15f
                                                ),
                                                new CryptoInsights(
                                                        "Polkadot",
                                                        "polkadot",
                                                        "40",
                                                        new BalancesResponse("160.40", "149.20", "0.0061208"),
                                                        1.92f
                                                ),
                                                new CryptoInsights(
                                                        "Uniswap",
                                                        "uniswap",
                                                        "30",
                                                        new BalancesResponse("127.50", "118.80", "0.0048591"),
                                                        1.52f
                                                ),
                                                new CryptoInsights(
                                                        "Polygon",
                                                        "matic-network",
                                                        "100",
                                                        new BalancesResponse("51.00", "47.54", "0.001947"),
                                                        0.61f
                                                ),
                                                new CryptoInsights(
                                                        "Cardano",
                                                        "cardano",
                                                        "150",
                                                        new BalancesResponse("37.34", "34.80", "0.001425"),
                                                        0.45f
                                                ),
                                                new CryptoInsights(
                                                        "Others",
                                                        new BalancesResponse("54.66", "50.99", "0.0020915"),
                                                        0.65f
                                                )
                                        )
                                )
                        )
                );
    }

    @Test
    void shouldRetrieveEmptyIfNoCryptosAreFoundForRetrieveCryptosBalancesInsights() {
        when(userCryptoServiceMock.findAll()).thenReturn(emptyList());

        var cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights();

        assertTrue(cryptosBalancesInsightsResponse.isEmpty());
    }

    @Test
    void shouldRetrieveUserCryptosInsights() {
        var cryptos = List.of("bitcoin", "litecoin");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("litecoin", "bitcoin"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("a76b400e-8ffc-42d6-bf47-db866eb20153", "163b1731-7a24-4e23-ac90-dc95ad8cb9e8")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosInsights = insightsService.retrieveUserCryptosInsights(0);

        assertThat(userCryptosInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                new PageUserCryptosInsightsResponse(
                                        1,
                                        1,
                                        false,
                                        new BalancesResponse("4689.34", "4226.75", "0.157352875"),
                                        List.of(
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                "Bitcoin",
                                                                "bitcoin",
                                                                "btc",
                                                                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        "0.15",
                                                        95.96f,
                                                        new BalancesResponse("4500.00", "4050.00", "0.15"
                                                        ),
                                                        new MarketData(
                                                                "19000000",
                                                                "21000000",
                                                                new CurrentPrice("30000", "27000", "1")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                "Litecoin",
                                                                "litecoin",
                                                                "ltc",
                                                                "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        "3.125",
                                                        4.04f,
                                                        new BalancesResponse("189.34", "176.75", "0.007352875"
                                                        ),
                                                        new MarketData(
                                                                "73638701",
                                                                "84000000",
                                                                new CurrentPrice("60.59", "56.56", "0.00235292")
                                                        ),
                                                        List.of("COINBASE")
                                                )
                                        )
                                )
                        )
                );
    }

    @Test
    void shouldRetrieveEmptyIfNoUserCryptosAreFoundForRetrieveUserCryptosInsights() {
        when(userCryptoServiceMock.findAll()).thenReturn(emptyList());

        var userCryptosInsights = insightsService.retrieveUserCryptosInsights(0);

        assertTrue(userCryptosInsights.isEmpty());
    }

    @Test
    void shouldRetrieveEmptyIfNoUserCryptosAreFoundForPageForRetrieveUserCryptosInsightsInsights() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(1);

        assertTrue(userCryptosPlatformsInsights.isEmpty());
    }

    @Test
    // shouldRetrieveUserCryptosPlatformsInsightsWithNextPage
    void test() {
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(
                Set.of(
                        "bitcoin",
                        "tether",
                        "ethereum",
                        "litecoin",
                        "binancecoin",
                        "ripple",
                        "cardano",
                        "polkadot",
                        "solana",
                        "matic-network",
                        "chainlink",
                        "dogecoin",
                        "avalanche-2",
                        "uniswap"
                )
        )).thenReturn(cryptos());

        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos());

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(0);

        assertTrue(userCryptosPlatformsInsights.isPresent());
        assertThat(userCryptosPlatformsInsights.get().cryptos()).hasSize(10);
        assertThat(userCryptosPlatformsInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                new PageUserCryptosInsightsResponse(
                                        1,
                                        2,
                                        true,
                                        new BalancesResponse("8373.63", "7663.61", "0.29959591932"),
                                        List.of(
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb38a-556e-11ee-b56e-325096b39f47",
                                                                "Bitcoin",
                                                                "bitcoin",
                                                                "btc",
                                                                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        "0.15",
                                                        53.74f,
                                                        new BalancesResponse("4500.00", "4050.00", "0.15"),
                                                        new MarketData(
                                                                "19000000",
                                                                "21000000",
                                                                new CurrentPrice("30000", "27000", "1")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fba74-556e-11ee-9bff-325096b39f47",
                                                                "Ethereum",
                                                                "ethereum",
                                                                "eth",
                                                                "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                                        ),
                                                        "1.112",
                                                        21.48f,
                                                        new BalancesResponse("1798.59", "1678.42", "0.06983755872"),
                                                        new MarketData(
                                                                "120220572",
                                                                "0",
                                                                new CurrentPrice("1617.44", "1509.37", "0.06280356")
                                                        ),
                                                        List.of("COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb696-556e-11ee-aa1c-325096b39f47",
                                                                "Ethereum",
                                                                "ethereum",
                                                                "eth",
                                                                "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                                        ),
                                                        "0.26",
                                                        5.02f,
                                                        new BalancesResponse("420.53", "392.44", "0.0163289256"),
                                                        new MarketData(
                                                                "120220572",
                                                                "0",
                                                                new CurrentPrice("1617.44", "1509.37", "0.06280356")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb9f2-556e-11ee-a929-325096b39f47",
                                                                "Avalanche",
                                                                "avalanche-2",
                                                                "avax",
                                                                "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574"
                                                        ),
                                                        "25",
                                                        2.78f,
                                                        new BalancesResponse("232.50", "216.75", "0.008879"),
                                                        new MarketData(
                                                                "353804673",
                                                                "720000000",
                                                                new CurrentPrice("9.3", "8.67", "0.00035516")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb768-556e-11ee-8b42-325096b39f47",
                                                                "BNB",
                                                                "binancecoin",
                                                                "bnb",
                                                                "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        "1",
                                                        2.53f,
                                                        new BalancesResponse("211.79", "197.80", "0.00811016"),
                                                        new MarketData(
                                                                "153856150",
                                                                "200000000",
                                                                new CurrentPrice("211.79", "197.8", "0.00811016")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb966-556e-11ee-81d6-325096b39f47",
                                                                "Chainlink",
                                                                "chainlink",
                                                                "link",
                                                                "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700"
                                                        ),
                                                        "35",
                                                        2.5f,
                                                        new BalancesResponse("209.65", "195.30", "0.0080031"),
                                                        new MarketData(
                                                                "538099971",
                                                                "1000000000",
                                                                new CurrentPrice("5.99", "5.58", "0.00022866")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb600-556e-11ee-83b6-325096b39f47",
                                                                "Tether",
                                                                "tether",
                                                                "usdt",
                                                                "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                                        ),
                                                        "200",
                                                        2.39f,
                                                        new BalancesResponse("199.92", "186.62", "0.00776"),
                                                        new MarketData(
                                                                "83016246102",
                                                                "0",
                                                                new CurrentPrice("0.999618", "0.933095", "0.0000388")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb70e-556e-11ee-8c2c-325096b39f47",
                                                                "Litecoin",
                                                                "litecoin",
                                                                "ltc",
                                                                "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        "3.125",
                                                        2.26f,
                                                        new BalancesResponse("189.34", "176.75", "0.007352875"),
                                                        new MarketData(
                                                                "73638701",
                                                                "84000000",
                                                                new CurrentPrice("60.59", "56.56", "0.00235292")
                                                        ),
                                                        List.of("COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb8e4-556e-11ee-883e-325096b39f47",
                                                                "Solana",
                                                                "solana",
                                                                "sol",
                                                                "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422"
                                                        ),
                                                        "10",
                                                        2.15f,
                                                        new BalancesResponse("180.40", "168.20", "0.0068809"),
                                                        new MarketData(
                                                                "410905807",
                                                                "0",
                                                                new CurrentPrice("18.04", "16.82", "0.00068809")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "676fb89e-556e-11ee-b0b8-325096b39f47",
                                                                "Polkadot",
                                                                "polkadot",
                                                                "dot",
                                                                "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644"
                                                        ),
                                                        "40",
                                                        1.92f,
                                                        new BalancesResponse("160.40", "149.20", "0.0061208"),
                                                        new MarketData(
                                                                "1274258350",
                                                                "0",
                                                                new CurrentPrice("4.01", "3.73", "0.00015302")
                                                        ),
                                                        List.of("COINBASE")
                                                )
                                        )
                                )
                        )
                );
    }

    @Test
    void shouldRetrieveUserCryptosPlatformsInsights() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0);

        assertThat(userCryptosPlatformsInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                new PageUserCryptosInsightsResponse(
                                        1,
                                        1,
                                        false,
                                        new BalancesResponse("6919.05", "6307.48", "0.24392648432"),
                                        List.of(
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Bitcoin",
                                                                "bitcoin",
                                                                "btc",
                                                                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        "0.15",
                                                        65.04f,
                                                        new BalancesResponse("4500.00", "4050.00", "0.15"),
                                                        new MarketData(
                                                                "19000000",
                                                                "21000000",
                                                                new CurrentPrice("30000", "27000", "1")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Ethereum",
                                                                "ethereum",
                                                                "eth",
                                                                "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                                        ),
                                                        "1.372",
                                                        32.07f,
                                                        new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                                        new MarketData(
                                                                "120220572",
                                                                "0",
                                                                new CurrentPrice("1617.44", "1509.37", "0.06280356")
                                                        ),
                                                        List.of("BINANCE", "COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Tether",
                                                                "tether",
                                                                "usdt",
                                                                "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                                        ),
                                                        "200",
                                                        2.89f,
                                                        new BalancesResponse("199.92", "186.62", "0.00776"),
                                                        new MarketData(
                                                                "83016246102",
                                                                "0",
                                                                new CurrentPrice("0.999618", "0.933095", "0.0000388")
                                                        ),
                                                        List.of("BINANCE")
                                                )
                                        )
                                )
                        )
                );
    }

    @Test
    void shouldRetrieveUserCryptosPlatformsInsightsWithNextPage() {
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(
                Set.of(
                        "bitcoin",
                        "tether",
                        "ethereum",
                        "litecoin",
                        "binancecoin",
                        "ripple",
                        "cardano",
                        "polkadot",
                        "solana",
                        "matic-network",
                        "chainlink",
                        "dogecoin",
                        "avalanche-2",
                        "uniswap"
                )
        )).thenReturn(cryptos());

        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos());

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0);

        assertTrue(userCryptosPlatformsInsights.isPresent());
        assertThat(userCryptosPlatformsInsights.get().cryptos()).hasSize(10);
        assertThat(userCryptosPlatformsInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                new PageUserCryptosInsightsResponse(
                                        1,
                                        2,
                                        true,
                                        new BalancesResponse("8373.63", "7663.61", "0.29959591932"),
                                        List.of(
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Bitcoin",
                                                                "bitcoin",
                                                                "btc",
                                                                "https://assets.coingecko.com/coins/images/1/large/bitcoin.png?1547033579"
                                                        ),
                                                        "0.15",
                                                        53.74f,
                                                        new BalancesResponse("4500.00", "4050.00", "0.15"),
                                                        new MarketData(
                                                                "19000000",
                                                                "21000000",
                                                                new CurrentPrice("30000", "27000", "1")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Ethereum",
                                                                "ethereum",
                                                                "eth",
                                                                "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                                        ),
                                                        "1.372",
                                                        26.5f,
                                                        new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                                        new MarketData(
                                                                "120220572",
                                                                "0",
                                                                new CurrentPrice("1617.44", "1509.37", "0.06280356")
                                                        ),
                                                        List.of("BINANCE", "COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Avalanche",
                                                                "avalanche-2",
                                                                "avax",
                                                                "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574"
                                                        ),
                                                        "25",
                                                        2.78f,
                                                        new BalancesResponse("232.50", "216.75", "0.008879"),
                                                        new MarketData(
                                                                "353804673",
                                                                "720000000",
                                                                new CurrentPrice("9.3", "8.67", "0.00035516")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "BNB",
                                                                "binancecoin",
                                                                "bnb",
                                                                "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850"
                                                        ),
                                                        "1",
                                                        2.53f,
                                                        new BalancesResponse("211.79", "197.80", "0.00811016"),
                                                        new MarketData(
                                                                "153856150",
                                                                "200000000",
                                                                new CurrentPrice("211.79", "197.8", "0.00811016")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Chainlink",
                                                                "chainlink",
                                                                "link",
                                                                "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700"
                                                        ),
                                                        "35",
                                                        2.5f,
                                                        new BalancesResponse("209.65", "195.30", "0.0080031"),
                                                        new MarketData(
                                                                "538099971",
                                                                "1000000000",
                                                                new CurrentPrice("5.99", "5.58", "0.00022866")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Tether",
                                                                "tether",
                                                                "usdt",
                                                                "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                                        ),
                                                        "200",
                                                        2.39f,
                                                        new BalancesResponse("199.92", "186.62", "0.00776"),
                                                        new MarketData(
                                                                "83016246102",
                                                                "0",
                                                                new CurrentPrice("0.999618", "0.933095", "0.0000388")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Litecoin",
                                                                "litecoin",
                                                                "ltc",
                                                                "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580"
                                                        ),
                                                        "3.125",
                                                        2.26f,
                                                        new BalancesResponse("189.34", "176.75", "0.007352875"),
                                                        new MarketData(
                                                                "73638701",
                                                                "84000000",
                                                                new CurrentPrice("60.59", "56.56", "0.00235292")
                                                        ),
                                                        List.of("COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Solana",
                                                                "solana",
                                                                "sol",
                                                                "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422"
                                                        ),
                                                        "10",
                                                        2.15f,
                                                        new BalancesResponse("180.40", "168.20", "0.0068809"),
                                                        new MarketData(
                                                                "410905807",
                                                                "0",
                                                                new CurrentPrice("18.04", "16.82", "0.00068809")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Polkadot",
                                                                "polkadot",
                                                                "dot",
                                                                "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644"
                                                        ),
                                                        "40",
                                                        1.92f,
                                                        new BalancesResponse("160.40", "149.20", "0.0061208"),
                                                        new MarketData(
                                                                "1274258350",
                                                                "0",
                                                                new CurrentPrice("4.01", "3.73", "0.00015302")
                                                        ),
                                                        List.of("COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Uniswap",
                                                                "uniswap",
                                                                "uni",
                                                                "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398"
                                                        ),
                                                        "30",
                                                        1.52f,
                                                        new BalancesResponse("127.50", "118.80", "0.0048591"),
                                                        new MarketData(
                                                                "753766667",
                                                                "1000000000",
                                                                new CurrentPrice("4.25", "3.96", "0.00016197")
                                                        ),
                                                        List.of("COINBASE")
                                                )
                                        )
                                )
                        )
                );
    }

    @Test
    void shouldRetrieveUserCryptosPlatformsInsightsForSecondPage() {
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(
                Set.of(
                        "bitcoin",
                        "tether",
                        "ethereum",
                        "litecoin",
                        "binancecoin",
                        "ripple",
                        "cardano",
                        "polkadot",
                        "solana",
                        "matic-network",
                        "chainlink",
                        "dogecoin",
                        "avalanche-2",
                        "uniswap"
                )
        )).thenReturn(cryptos());
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos());

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(1);

        assertThat(userCryptosPlatformsInsights)
                .usingRecursiveComparison()
                .isEqualTo(
                        Optional.of(
                                new PageUserCryptosInsightsResponse(
                                        2,
                                        2,
                                        false,
                                        new BalancesResponse("8373.63", "7663.61", "0.29959591932"),
                                        List.of(
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Polygon",
                                                                "matic-network",
                                                                "matic",
                                                                "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912"
                                                        ),
                                                        "100",
                                                        0.61f,
                                                        new BalancesResponse("51.00", "47.54", "0.001947"),
                                                        new MarketData(
                                                                "9319469069",
                                                                "10000000000",
                                                                new CurrentPrice("0.509995", "0.475407", "0.00001947")
                                                        ),
                                                        List.of("COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Cardano",
                                                                "cardano",
                                                                "ada",
                                                                "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860"
                                                        ),
                                                        "150",
                                                        0.45f,
                                                        new BalancesResponse("37.34", "34.80", "0.001425"),
                                                        new MarketData(
                                                                "35045020830",
                                                                "45000000000",
                                                                new CurrentPrice("0.248915", "0.231985", "0.0000095")
                                                        ),
                                                        List.of("BINANCE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "Dogecoin",
                                                                "dogecoin",
                                                                "doge",
                                                                "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256"
                                                        ),
                                                        "500",
                                                        0.37f,
                                                        new BalancesResponse("30.74", "28.66", "0.001175"),
                                                        new MarketData(
                                                                "140978466383",
                                                                "0",
                                                                new CurrentPrice("0.061481", "0.057319", "0.00000235")
                                                        ),
                                                        List.of("COINBASE")
                                                ),
                                                new UserCryptosInsights(
                                                        new CryptoInfo(
                                                                "XRP",
                                                                "ripple",
                                                                "xrp",
                                                                "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731"
                                                        ),
                                                        "50",
                                                        0.29f,
                                                        new BalancesResponse("23.92", "22.33", "0.0009165"),
                                                        new MarketData(
                                                                "53083046512",
                                                                "100000000000",
                                                                new CurrentPrice("0.478363", "0.446699", "0.00001833")
                                                        ),
                                                        List.of("COINBASE")
                                                )
                                        )
                                )
                        )
                );
    }

    @Test
    void shouldRetrieveEmptyIfNoUserCryptosAreFoundForRetrieveUserCryptosPlatformsInsights() {
        when(userCryptoServiceMock.findAll()).thenReturn(emptyList());

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0);

        assertTrue(userCryptosPlatformsInsights.isEmpty());
    }

    @Test
    void shouldRetrieveEmptyIfNoUserCryptosAreFoundForPageForRetrieveUserCryptosPlatformsInsights() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.coingeckoCryptoId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.id())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
                .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(1);

        assertTrue(userCryptosPlatformsInsights.isEmpty());
    }

    private List<UserCrypto> userCryptos() {
        return List.of(
                new UserCrypto(
                        "676fb38a-556e-11ee-b56e-325096b39f47", "bitcoin", new BigDecimal("0.15"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fb600-556e-11ee-83b6-325096b39f47", "tether", new BigDecimal("200"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fb696-556e-11ee-aa1c-325096b39f47", "ethereum", new BigDecimal("0.26"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fba74-556e-11ee-9bff-325096b39f47", "ethereum", new BigDecimal("1.112"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                ),
                new UserCrypto(
                        "676fb70e-556e-11ee-8c2c-325096b39f47", "litecoin", new BigDecimal("3.125"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                ),
                new UserCrypto(
                        "676fb768-556e-11ee-8b42-325096b39f47", "binancecoin", new BigDecimal("1"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fb7c2-556e-11ee-9800-325096b39f47", "ripple", new BigDecimal("50"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                ),
                new UserCrypto(
                        "676fb83a-556e-11ee-9731-325096b39f47", "cardano", new BigDecimal("150"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fb89e-556e-11ee-b0b8-325096b39f47", "polkadot", new BigDecimal("40"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                ),
                new UserCrypto(
                        "676fb8e4-556e-11ee-883e-325096b39f47", "solana", new BigDecimal("10"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fb92a-556e-11ee-9de1-325096b39f47", "matic-network", new BigDecimal("100"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                ),
                new UserCrypto(
                        "676fb966-556e-11ee-81d6-325096b39f47", "chainlink", new BigDecimal("35"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fb9ac-556e-11ee-b4fa-325096b39f47", "dogecoin", new BigDecimal("500"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                ),
                new UserCrypto(
                        "676fb9f2-556e-11ee-a929-325096b39f47", "avalanche-2", new BigDecimal("25"), "163b1731-7a24-4e23-ac90-dc95ad8cb9e8"
                ),
                new UserCrypto(
                        "676fba2e-556e-11ee-a181-325096b39f47", "uniswap", new BigDecimal("30"), "a76b400e-8ffc-42d6-bf47-db866eb20153"
                )
        );
    }

    private List<Crypto> cryptos() {
        var localDateTime = LocalDateTime.now();

        return List.of(
                getCryptoEntity(),
                new Crypto(
                        "tether",
                        "Tether",
                        "usdt",
                        "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
                        new BigDecimal("0.999618"),
                        new BigDecimal("0.933095"),
                        new BigDecimal("0.0000388"),
                        new BigDecimal("83016246102"),
                        BigDecimal.ZERO,
                        localDateTime
                ),
                new Crypto(
                        "ethereum",
                        "Ethereum",
                        "eth",
                        "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
                        new BigDecimal("1617.44"),
                        new BigDecimal("1509.37"),
                        new BigDecimal("0.06280356"),
                        new BigDecimal("120220572"),
                        BigDecimal.ZERO,
                        localDateTime
                ),
                new Crypto(
                        "litecoin",
                        "Litecoin",
                        "ltc",
                        "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580",
                        new BigDecimal("60.59"),
                        new BigDecimal("56.56"),
                        new BigDecimal("0.00235292"),
                        new BigDecimal("73638701"),
                        new BigDecimal("84000000"),
                        localDateTime
                ),
                new Crypto(
                        "binancecoin",
                        "BNB",
                        "bnb",
                        "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850",
                        new BigDecimal("211.79"),
                        new BigDecimal("197.8"),
                        new BigDecimal("0.00811016"),
                        new BigDecimal("153856150"),
                        new BigDecimal("200000000"),
                        localDateTime
                ),
                new Crypto(
                        "ripple",
                        "XRP",
                        "xrp",
                        "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731",
                        new BigDecimal("0.478363"),
                        new BigDecimal("0.446699"),
                        new BigDecimal("0.00001833"),
                        new BigDecimal("53083046512"),
                        new BigDecimal("100000000000"),
                        localDateTime
                ),
                new Crypto(
                        "cardano",
                        "Cardano",
                        "ada",
                        "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860",
                        new BigDecimal("0.248915"),
                        new BigDecimal("0.231985"),
                        new BigDecimal("0.0000095"),
                        new BigDecimal("35045020830"),
                        new BigDecimal("45000000000"),
                        localDateTime
                ),
                new Crypto(
                        "polkadot",
                        "Polkadot",
                        "dot",
                        "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
                        new BigDecimal("4.01"),
                        new BigDecimal("3.73"),
                        new BigDecimal("0.00015302"),
                        new BigDecimal("1274258350"),
                        BigDecimal.ZERO,
                        localDateTime
                ),
                new Crypto(
                        "solana",
                        "Solana",
                        "sol",
                        "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422",
                        new BigDecimal("18.04"),
                        new BigDecimal("16.82"),
                        new BigDecimal("0.00068809"),
                        new BigDecimal("410905807"),
                        BigDecimal.ZERO,
                        localDateTime
                ),
                new Crypto(
                        "matic-network",
                        "Polygon",
                        "matic",
                        "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912",
                        new BigDecimal("0.509995"),
                        new BigDecimal("0.475407"),
                        new BigDecimal("0.00001947"),
                        new BigDecimal("9319469069"),
                        new BigDecimal("10000000000"),
                        localDateTime
                ),
                new Crypto(
                        "chainlink",
                        "Chainlink",
                        "link",
                        "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700",
                        new BigDecimal("5.99"),
                        new BigDecimal("5.58"),
                        new BigDecimal("0.00022866"),
                        new BigDecimal("538099971"),
                        new BigDecimal("1000000000"),
                        localDateTime
                ),
                new Crypto(
                        "dogecoin",
                        "Dogecoin",
                        "doge",
                        "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256",
                        new BigDecimal("0.061481"),
                        new BigDecimal("0.057319"),
                        new BigDecimal("0.00000235"),
                        new BigDecimal("140978466383"),
                        BigDecimal.ZERO,
                        localDateTime
                ),
                new Crypto(
                        "avalanche-2",
                        "Avalanche",
                        "avax",
                        "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574",
                        new BigDecimal("9.3"),
                        new BigDecimal("8.67"),
                        new BigDecimal("0.00035516"),
                        new BigDecimal("353804673"),
                        new BigDecimal("720000000"),
                        localDateTime
                ),
                new Crypto(
                        "uniswap",
                        "Uniswap",
                        "uni",
                        "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398",
                        new BigDecimal("4.25"),
                        new BigDecimal("3.96"),
                        new BigDecimal("0.00016197"),
                        new BigDecimal("753766667"),
                        new BigDecimal("1000000000"),
                        localDateTime
                )
        );
    }

}