package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.ChangePercentages;
import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import com.distasilucas.cryptobalancetracker.entity.LastKnownPrices;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
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
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static com.distasilucas.cryptobalancetracker.TestDataSource.getBinancePlatformEntity;
import static com.distasilucas.cryptobalancetracker.TestDataSource.getBitcoinCryptoEntity;
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

    @Mock
    private DateBalanceRepository dateBalanceRepositoryMock;

    @Mock
    private Clock clockMock;

    private InsightsService insightsService;

    private static final SortParams sortParams = new SortParams(SortBy.PERCENTAGE, SortType.DESC);
    private static final LocalDateTime localDateTime = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        openMocks(this);
        insightsService = new InsightsService(12, platformServiceMock, userCryptoServiceMock, cryptoServiceMock,
            dateBalanceRepositoryMock, clockMock);
    }

    @Test
    void shouldRetrieveTotalBalancesInsights() {
        var cryptos = List.of("bitcoin", "tether", "ethereum", "litecoin");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();

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
    void shouldRetrieveDateBalancesForOneDay() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var balances = List.of(
            new DateBalance("", now.minusDays(1), "900"),
            new DateBalance("", now, "1000")
        );

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findDateBalancesByDateBetween(now.minusDays(2), now)).thenReturn(balances);

        var datesBalances = insightsService.retrieveDatesBalances(DateRange.ONE_DAY);

        assertThat(datesBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("7 February 2024", "900"),
                        new DatesBalances("8 February 2024", "1000")
                    ),
                    11.11F,
                    "100"
                )
            ));
    }

    @Test
    void shouldRetrieveDateBalancesForThreeDays() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var balances = List.of(
            new DateBalance("", now.minusDays(2), "1100"),
            new DateBalance("", now.minusDays(1), "900"),
            new DateBalance("", now, "1000")
        );

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findDateBalancesByDateBetween(now.minusDays(3), now)).thenReturn(balances);

        var datesBalances = insightsService.retrieveDatesBalances(DateRange.THREE_DAYS);

        assertThat(datesBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("6 February 2024", "1100"),
                        new DatesBalances("7 February 2024", "900"),
                        new DatesBalances("8 February 2024", "1000")
                    ),
                    -9.09F,
                    "-100"
                )
            ));
    }

    @Test
    void shouldRetrieveDatesBalancesForOneWeek() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var balances = List.of(
            new DateBalance("", now.minusDays(5), "1500"),
            new DateBalance("", now.minusDays(4), "1250.75"),
            new DateBalance("", now.minusDays(3), "900"),
            new DateBalance("", now, "1000")
        );

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findDateBalancesByDateBetween(now.minusWeeks(1), now))
            .thenReturn(balances);

        var dateBalances = insightsService.retrieveDatesBalances(DateRange.ONE_WEEK);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("3 February 2024", "1500"),
                        new DatesBalances("4 February 2024", "1250.75"),
                        new DatesBalances("5 February 2024", "900"),
                        new DatesBalances("8 February 2024", "1000")
                    ),
                    -33.33F,
                    "-500"
                )
            ));
    }

    @Test
    void shouldRetrieveDateBalancesForOneMonth() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var balances = List.of(
            new DateBalance("", now.minusDays(14), "1350"),
            new DateBalance("", now.minusDays(12), "1450"),
            new DateBalance("", now.minusDays(10), "1250"),
            new DateBalance("", now.minusDays(8), "1450"),
            new DateBalance("", now.minusDays(6), "1500"),
            new DateBalance("", now.minusDays(4), "1500"),
            new DateBalance("", now.minusDays(2), "900"),
            new DateBalance("", now, "1000")
        );
        var dates = getMockDates(now, 16, 2);

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findAllByDateIn(dates)).thenReturn(balances);

        var dateBalances = insightsService.retrieveDatesBalances(DateRange.ONE_MONTH);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("25 January 2024", "1350"),
                        new DatesBalances("27 January 2024", "1450"),
                        new DatesBalances("29 January 2024", "1250"),
                        new DatesBalances("31 January 2024", "1450"),
                        new DatesBalances("2 February 2024", "1500"),
                        new DatesBalances("4 February 2024", "1500"),
                        new DatesBalances("6 February 2024", "900"),
                        new DatesBalances("8 February 2024", "1000")
                    ),
                    -25.93F,
                    "-350"
                )
            ));
    }

    @Test
    void shouldRetrieveDateBalancesForThreeMonths() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var balances = List.of(
            new DateBalance("", now.minusDays(42), "1400"),
            new DateBalance("", now.minusDays(36), "1350"),
            new DateBalance("", now.minusDays(30), "1250"),
            new DateBalance("", now.minusDays(24), "1150"),
            new DateBalance("", now.minusDays(18), "1200"),
            new DateBalance("", now.minusDays(12), "1100"),
            new DateBalance("", now.minusDays(6), "900"),
            new DateBalance("", now, "1000")
        );
        var dates = getMockDates(now, 16, 6);

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findAllByDateIn(dates)).thenReturn(balances);

        var dateBalances = insightsService.retrieveDatesBalances(DateRange.THREE_MONTHS);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("28 December 2023", "1400"),
                        new DatesBalances("3 January 2024", "1350"),
                        new DatesBalances("9 January 2024", "1250"),
                        new DatesBalances("15 January 2024", "1150"),
                        new DatesBalances("21 January 2024", "1200"),
                        new DatesBalances("27 January 2024", "1100"),
                        new DatesBalances("2 February 2024", "900"),
                        new DatesBalances("8 February 2024", "1000")
                    ),
                    -28.57F,
                    "-400"
                )
            ));
    }

    @Test
    void shouldRetrieveDateBalancesForSixMonths() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var balances = List.of(
            new DateBalance("", now.minusDays(70), "1400"),
            new DateBalance("", now.minusDays(60), "1350"),
            new DateBalance("", now.minusDays(50), "1250"),
            new DateBalance("", now.minusDays(40), "1150"),
            new DateBalance("", now.minusDays(30), "1200"),
            new DateBalance("", now.minusDays(20), "1100"),
            new DateBalance("", now.minusDays(10), "900"),
            new DateBalance("", now, "1000")
        );
        var dates = getMockDates(now, 19, 10);

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findAllByDateIn(dates)).thenReturn(balances);

        var dateBalances = insightsService.retrieveDatesBalances(DateRange.SIX_MONTHS);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("30 November 2023", "1400"),
                        new DatesBalances("10 December 2023", "1350"),
                        new DatesBalances("20 December 2023", "1250"),
                        new DatesBalances("30 December 2023", "1150"),
                        new DatesBalances("9 January 2024", "1200"),
                        new DatesBalances("19 January 2024", "1100"),
                        new DatesBalances("29 January 2024", "900"),
                        new DatesBalances("8 February 2024", "1000")
                    ),
                    -28.57F,
                    "-400"
                )
            ));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ONE_MONTH", "THREE_MONTHS", "SIX_MONTHS", "ONE_YEAR"})
    void shouldRetrieveLastTwelveDaysBalancesIfRequiredLengthIsNotMeet(String range) {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var dateRange = DateRange.valueOf(range);
        var balances = List.of(
            new DateBalance("", now.minusDays(2), "1100"),
            new DateBalance("", now.minusDays(1), "900"),
            new DateBalance("", now, "1000")
        );
        var dates = getMockDates(now, 19, 10);
        var twelveDaysBefore = now.minusDays(12);

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findAllByDateIn(dates)).thenReturn(balances);
        when(dateBalanceRepositoryMock.findDateBalancesByDateBetween(twelveDaysBefore, now))
            .thenReturn(retrieveLastTwelveDaysBalances());

        var dateBalances = insightsService.retrieveDatesBalances(dateRange);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("5 March 2024", "1000"),
                        new DatesBalances("6 March 2024", "850"),
                        new DatesBalances("7 March 2024", "900"),
                        new DatesBalances("8 March 2024", "1150"),
                        new DatesBalances("9 March 2024", "1050"),
                        new DatesBalances("10 March 2024", "1200"),
                        new DatesBalances("11 March 2024", "1150")
                    ),
                    15F,
                    "150"
                )
            ));
    }

    @Test
    void shouldRetrieveDateBalancesForOneYear() {
        var now = LocalDate.of(2024, 2, 8);
        var zonedDateTime = now.atStartOfDay().atZone(ZoneOffset.UTC);
        var dates = getMockDates(now);
        var balances = List.of(
            new DateBalance("", now.minusMonths(7), "1000"),
            new DateBalance("", now.minusMonths(6), "1300"),
            new DateBalance("", now.minusMonths(5), "1400"),
            new DateBalance("", now.minusMonths(4), "950"),
            new DateBalance("", now.minusMonths(3), "1110"),
            new DateBalance("", now.minusMonths(2), "1250"),
            new DateBalance("", now.minusMonths(1), "900"),
            new DateBalance("", now, "1400")
        );

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findAllByDateIn(dates)).thenReturn(balances);

        var dateBalances = insightsService.retrieveDatesBalances(DateRange.ONE_YEAR);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(
                new DatesBalanceResponse(
                    List.of(
                        new DatesBalances("8 July 2023", "1000"),
                        new DatesBalances("8 August 2023", "1300"),
                        new DatesBalances("8 September 2023", "1400"),
                        new DatesBalances("8 October 2023", "950"),
                        new DatesBalances("8 November 2023", "1110"),
                        new DatesBalances("8 December 2023", "1250"),
                        new DatesBalances("8 January 2024", "900"),
                        new DatesBalances("8 February 2024", "1400")
                    ),
                    40,
                    "400"
                )
            ));

    }

    @Test
    void shouldRetrieveEmptyDatesBalances() {
        var dateFrom = LocalDate.of(2024, 2, 1);
        var dateTo = LocalDate.of(2024, 2, 8);
        var zonedDateTime = dateFrom.atStartOfDay().atZone(ZoneOffset.UTC);

        when(clockMock.instant()).thenReturn(zonedDateTime.toInstant());
        when(clockMock.getZone()).thenReturn(zonedDateTime.getZone());
        when(dateBalanceRepositoryMock.findDateBalancesByDateBetween(dateFrom, dateTo))
            .thenReturn(Collections.emptyList());

        var dateBalances = insightsService.retrieveDatesBalances(DateRange.ONE_WEEK);

        assertThat(dateBalances)
            .usingRecursiveComparison()
            .isEqualTo(Optional.empty());
    }

    @Test
    void shouldRetrievePlatformInsightsWithOneCrypto() {
        var platformEntity = getBinancePlatformEntity();
        var userCryptos = getUserCrypto();
        var bitcoinCryptoEntity = getBitcoinCryptoEntity();

        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")).thenReturn(platformEntity);
        when(userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111")).thenReturn(List.of(userCryptos));
        when(cryptoServiceMock.findAllByIds(List.of("bitcoin"))).thenReturn(List.of(bitcoinCryptoEntity));

        var platformInsightsResponse = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        var expected = new PlatformInsightsResponse(
            "BINANCE",
            new BalancesResponse("7500.00", "6750.00", "0.25"),
            List.of(
                new CryptoInsights(
                    "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                    "Bitcoin",
                    "bitcoin",
                    "0.25",
                    new BalancesResponse("7500.00", "6750.00", "0.25"),
                    100f
                )
            )
        );
        assertThat(platformInsightsResponse)
            .usingRecursiveComparison()
            .isEqualTo(Optional.of(expected));
    }

    @Test
    void shouldRetrievePlatformInsightsWithMultipleCryptos() {
        var platformEntity = getBinancePlatformEntity();
        var bitcoinUserCrypto = getUserCrypto();
        var polkadotUserCrypto = new UserCrypto("1ad5b2fe-6060-48b5-aa02-3557e1d6e40b", new BigDecimal("100"), getBinancePlatformEntity(), getPolkadotCrypto());

        when(platformServiceMock.retrievePlatformById("123e4567-e89b-12d3-a456-426614174111")).thenReturn(platformEntity);
        when(userCryptoServiceMock.findAllByPlatformId("123e4567-e89b-12d3-a456-426614174111"))
            .thenReturn(List.of(bitcoinUserCrypto, polkadotUserCrypto));
        when(cryptoServiceMock.findAllByIds(List.of("bitcoin", "polkadot")))
            .thenReturn(List.of(getBitcoinCryptoEntity(), getPolkadotCrypto()));

        var platformInsightsResponse = insightsService.retrievePlatformInsights("123e4567-e89b-12d3-a456-426614174111");

        var expected = new PlatformInsightsResponse(
            "BINANCE",
            new BalancesResponse("7901.00", "7123.00", "0.265302"),
            List.of(
                new CryptoInsights(
                    "af827ac7-d642-4461-a73c-b31ca6f6d13d",
                    "Bitcoin",
                    "bitcoin",
                    "0.25",
                    new BalancesResponse("7500.00", "6750.00", "0.25"),
                    94.92f
                ),
                new CryptoInsights(
                    polkadotUserCrypto.getId(),
                    "Polkadot",
                    "polkadot",
                    "100",
                    new BalancesResponse("401.00", "373.00", "0.015302"),
                    5.08f
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
        var bitcoinCryptoEntity = getBitcoinCryptoEntity();

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
        var binancePlatform = new Platform("4f663841-7c82-4d0f-a756-cf7d4e2d3bc6", "BINANCE");
        var coinbasePlatform = new Platform("fa3db02d-4d43-416a-951b-e7ea3a4fe386", "COINBASE");
        var bitcoinUserCrypto = List.of(
            getUserCrypto(),
            new UserCrypto(
                "ed34425b-d9f7-4244-bd16-0212621848c6",
                new BigDecimal("0.03455"),
                coinbasePlatform,
                getBitcoinCryptoEntity()
            )
        );
        var bitcoinCryptoEntity = getBitcoinCryptoEntity();

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
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
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
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();

        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);
        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum", "litecoin"))).thenReturn(cryptosEntities);

        var cryptosBalancesInsightsResponse = insightsService.retrieveCryptosBalancesInsights();

        var expected = new CryptosBalancesInsightsResponse(
            new BalancesResponse("7108.39", "6484.23", "0.25127935932"),
            List.of(
                new CryptoInsights(
                    null,
                    "Bitcoin",
                    "bitcoin",
                    "0.15",
                    new BalancesResponse("4500.00", "4050.00", "0.15"),
                    63.31f
                ),
                new CryptoInsights(
                    null,
                    "Ethereum",
                    "ethereum",
                    "1.372",
                    new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                    31.22f
                ),
                new CryptoInsights(
                    null,
                    "Tether",
                    "tether",
                    "200",
                    new BalancesResponse("199.92", "186.62", "0.00776"),
                    2.81f
                ),
                new CryptoInsights(
                    null,
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
                                null,
                                "Bitcoin",
                                "bitcoin",
                                "0.15",
                                new BalancesResponse("4500.00", "4050.00", "0.15"),
                                53.74f
                            ),
                            new CryptoInsights(
                                null,
                                "Ethereum",
                                "ethereum",
                                "1.372",
                                new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                26.5f
                            ),
                            new CryptoInsights(
                                null,
                                "Avalanche",
                                "avalanche-2",
                                "25",
                                new BalancesResponse("232.50", "216.75", "0.008879"),
                                2.78f
                            ),
                            new CryptoInsights(
                                null,
                                "BNB",
                                "binancecoin",
                                "1",
                                new BalancesResponse("211.79", "197.80", "0.00811016"),
                                2.53f
                            ),
                            new CryptoInsights(
                                null,
                                "Chainlink",
                                "chainlink",
                                "35",
                                new BalancesResponse("209.65", "195.30", "0.0080031"),
                                2.5f
                            ),
                            new CryptoInsights(
                                null,
                                "Tether",
                                "tether",
                                "200",
                                new BalancesResponse("199.92", "186.62", "0.00776"),
                                2.39f
                            ),
                            new CryptoInsights(
                                null,
                                "Litecoin",
                                "litecoin",
                                "3.125",
                                new BalancesResponse("189.34", "176.75", "0.007352875"),
                                2.26f
                            ),
                            new CryptoInsights(
                                null,
                                "Solana",
                                "solana",
                                "10",
                                new BalancesResponse("180.40", "168.20", "0.0068809"),
                                2.15f
                            ),
                            new CryptoInsights(
                                null,
                                "Polkadot",
                                "polkadot",
                                "40",
                                new BalancesResponse("160.40", "149.20", "0.0061208"),
                                1.92f
                            ),
                            new CryptoInsights(
                                null,
                                "Uniswap",
                                "uniswap",
                                "30",
                                new BalancesResponse("127.50", "118.80", "0.0048591"),
                                1.52f
                            ),
                            new CryptoInsights(
                                null,
                                "Polygon",
                                "matic-network",
                                "100",
                                new BalancesResponse("51.00", "47.54", "0.001947"),
                                0.61f
                            ),
                            new CryptoInsights(
                                null,
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
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("litecoin", "bitcoin"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("a76b400e-8ffc-42d6-bf47-db866eb20153", "163b1731-7a24-4e23-ac90-dc95ad8cb9e8")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, sortParams);

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
                                new BalancesResponse("189.34", "176.75", "0.007352875"),
                                19,
                                new MarketData(
                                    new CirculatingSupply("73638701", 87.67f),
                                    "84000000",
                                    new CurrentPrice("60.59", "56.56", "0.00235292"),
                                    "5259205267",
                                    new PriceChange(
                                        new BigDecimal("6.00"),
                                        new BigDecimal("-2.00"),
                                        new BigDecimal("12.00")
                                    )
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

        var userCryptosInsights = insightsService.retrieveUserCryptosInsights(0, sortParams);

        assertTrue(userCryptosInsights.isEmpty());
    }

    @Test
    void shouldRetrieveEmptyIfNoUserCryptosAreFoundForPageForRetrieveUserCryptosInsightsInsights() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(1, sortParams);

        assertTrue(userCryptosPlatformsInsights.isEmpty());
    }

    @Test
    void shouldRetrieveUserCryptosInsightsWithNextPage() {
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");
        var ethereumMarketData = new MarketData(
            new CirculatingSupply("120220572", 0),
            "0",
            new CurrentPrice("1617.44", "1509.37", "0.06280356"),
            "298219864117",
            new PriceChange(
                new BigDecimal("10.00"),
                new BigDecimal("-5.00"),
                new BigDecimal("2.00")
            )
        );

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

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosInsights(0, sortParams);

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
                                2,
                                ethereumMarketData,
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
                                2,
                                ethereumMarketData,
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
                                10,
                                new MarketData(
                                    new CirculatingSupply("353804673", 49.14f),
                                    "720000000",
                                    new CurrentPrice("9.3", "8.67", "0.00035516"),
                                    "11953262327",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("1.00"),
                                        new BigDecimal("8.00")
                                    )
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
                                4,
                                new MarketData(
                                    new CirculatingSupply("153856150", 76.93f),
                                    "200000000",
                                    new CurrentPrice("211.79", "197.8", "0.00811016"),
                                    "48318686968",
                                    new PriceChange(
                                        new BigDecimal("6.00"),
                                        new BigDecimal("-2.00"),
                                        new BigDecimal("12.00")
                                    )
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
                                14,
                                new MarketData(
                                    new CirculatingSupply("538099971", 53.81f),
                                    "1000000000",
                                    new CurrentPrice("5.99", "5.58", "0.00022866"),
                                    "9021587267",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("-1.00"),
                                        new BigDecimal("8.00")
                                    )
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
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
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
                                19,
                                new MarketData(
                                    new CirculatingSupply("73638701", 87.67f),
                                    "84000000",
                                    new CurrentPrice("60.59", "56.56", "0.00235292"),
                                    "5259205267",
                                    new PriceChange(
                                        new BigDecimal("6.00"),
                                        new BigDecimal("-2.00"),
                                        new BigDecimal("12.00")
                                    )
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
                                5,
                                new MarketData(
                                    new CirculatingSupply("410905807", 0),
                                    "0",
                                    new CurrentPrice("18.04", "16.82", "0.00068809"),
                                    "40090766907",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("1.00"),
                                        new BigDecimal("-2.00")
                                    )
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
                                13,
                                new MarketData(
                                    new CirculatingSupply("1274258350", 0),
                                    "0",
                                    new CurrentPrice("4.01", "3.73", "0.00015302"),
                                    "8993575127",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("-1.00"),
                                        new BigDecimal("2.00")
                                    )
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
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, sortParams);

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
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
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
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
                                ),
                                List.of("BINANCE")
                            )
                        )
                    )
                )
            );
    }

    @Test
    void shouldRetrieveUserCryptosInsightsSortedByCurrentPriceAscending() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, new SortParams(SortBy.CURRENT_PRICE, SortType.ASC));

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
                                    "Tether",
                                    "tether",
                                    "usdt",
                                    "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                ),
                                "200",
                                2.89f,
                                new BalancesResponse("199.92", "186.62", "0.00776"),
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
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
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
                                ),
                                List.of("BINANCE", "COINBASE")
                            ),
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
                                List.of("BINANCE")
                            )
                        )
                    )
                )
            );
    }

    @Test
    void shouldRetrieveUserCryptosInsightsSortedByMaxSupplyAscending() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, new SortParams(SortBy.MAX_SUPPLY, SortType.ASC));

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
                                    "Ethereum",
                                    "ethereum",
                                    "eth",
                                    "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                ),
                                "1.372",
                                32.07f,
                                new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
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
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
                                ),
                                List.of("BINANCE")
                            ),
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
                                List.of("BINANCE")
                            )
                        )
                    )
                )
            );
    }

    @Test
    void shouldRetrieveUserCryptosInsightsSortedByChangePriceIn24hDescending() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, new SortParams(SortBy.CHANGE_PRICE_IN_24H, SortType.DESC));

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
                                    "Ethereum",
                                    "ethereum",
                                    "eth",
                                    "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880"
                                ),
                                "1.372",
                                32.07f,
                                new BalancesResponse("2219.13", "2070.86", "0.08616648432"),
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
                                ),
                                List.of("BINANCE", "COINBASE")
                            ),
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
                                2.89f,
                                new BalancesResponse("199.92", "186.62", "0.00776"),
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
                                ),
                                List.of("BINANCE")
                            )
                        )
                    )
                )
            );
    }

    @Test
    void shouldRetrieveUserCryptosInsightsSortedByChangePriceIn7dDescending() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, new SortParams(SortBy.CHANGE_PRICE_IN_7D, SortType.DESC));

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
                                    "Tether",
                                    "tether",
                                    "usdt",
                                    "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                ),
                                "200",
                                2.89f,
                                new BalancesResponse("199.92", "186.62", "0.00776"),
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
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
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
                                ),
                                List.of("BINANCE", "COINBASE")
                            ),
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
                                List.of("BINANCE")
                            )
                        )
                    )
                )
            );
    }

    @Test
    void shouldRetrieveUserCryptosInsightsSortedByChangePriceIn30dAscending() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, new SortParams(SortBy.CHANGE_PRICE_IN_30D, SortType.ASC));

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
                                    "Tether",
                                    "tether",
                                    "usdt",
                                    "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663"
                                ),
                                "200",
                                2.89f,
                                new BalancesResponse("199.92", "186.62", "0.00776"),
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
                                ),
                                List.of("BINANCE")
                            ),
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
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
                                ),
                                List.of("BINANCE", "COINBASE")
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

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, sortParams);

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
                                2,
                                new MarketData(
                                    new CirculatingSupply("120220572", 0),
                                    "0",
                                    new CurrentPrice("1617.44", "1509.37", "0.06280356"),
                                    "298219864117",
                                    new PriceChange(
                                        new BigDecimal("10.00"),
                                        new BigDecimal("-5.00"),
                                        new BigDecimal("2.00")
                                    )
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
                                10,
                                new MarketData(
                                    new CirculatingSupply("353804673", 49.14f),
                                    "720000000",
                                    new CurrentPrice("9.3", "8.67", "0.00035516"),
                                    "11953262327",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("1.00"),
                                        new BigDecimal("8.00")
                                    )
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
                                4,
                                new MarketData(
                                    new CirculatingSupply("153856150", 76.93f),
                                    "200000000",
                                    new CurrentPrice("211.79", "197.8", "0.00811016"),
                                    "48318686968",
                                    new PriceChange(
                                        new BigDecimal("6.00"),
                                        new BigDecimal("-2.00"),
                                        new BigDecimal("12.00")
                                    )
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
                                14,
                                new MarketData(
                                    new CirculatingSupply("538099971", 53.81f),
                                    "1000000000",
                                    new CurrentPrice("5.99", "5.58", "0.00022866"),
                                    "9021587267",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("-1.00"),
                                        new BigDecimal("8.00")
                                    )
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
                                3,
                                new MarketData(
                                    new CirculatingSupply("83016246102", 0),
                                    "0",
                                    new CurrentPrice("0.999618", "0.933095", "0.0000388"),
                                    "95085861049",
                                    new PriceChange(
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00"),
                                        new BigDecimal("0.00")
                                    )
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
                                19,
                                new MarketData(
                                    new CirculatingSupply("73638701", 87.67f),
                                    "84000000",
                                    new CurrentPrice("60.59", "56.56", "0.00235292"),
                                    "5259205267",
                                    new PriceChange(
                                        new BigDecimal("6.00"),
                                        new BigDecimal("-2.00"),
                                        new BigDecimal("12.00")
                                    )
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
                                5,
                                new MarketData(
                                    new CirculatingSupply("410905807", 0),
                                    "0",
                                    new CurrentPrice("18.04", "16.82", "0.00068809"),
                                    "40090766907",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("1.00"),
                                        new BigDecimal("-2.00")
                                    )
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
                                13,
                                new MarketData(
                                    new CirculatingSupply("1274258350", 0),
                                    "0",
                                    new CurrentPrice("4.01", "3.73", "0.00015302"),
                                    "8993575127",
                                    new PriceChange(
                                        new BigDecimal("4.00"),
                                        new BigDecimal("-1.00"),
                                        new BigDecimal("2.00")
                                    )
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
                                22,
                                new MarketData(
                                    new CirculatingSupply("753766667", 75.38f),
                                    "1000000000",
                                    new CurrentPrice("4.25", "3.96", "0.00016197"),
                                    "4772322900",
                                    new PriceChange(
                                        new BigDecimal("2.00"),
                                        new BigDecimal("-1.00"),
                                        new BigDecimal("3.00")
                                    )
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

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(1, sortParams);

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
                                16,
                                new MarketData(
                                    new CirculatingSupply("9319469069", 93.19f),
                                    "10000000000",
                                    new CurrentPrice("0.509995", "0.475407", "0.00001947"),
                                    "7001911961",
                                    new PriceChange(
                                        new BigDecimal("14.00"),
                                        new BigDecimal("-10.00"),
                                        new BigDecimal("2.00")
                                    )
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
                                9,
                                new MarketData(
                                    new CirculatingSupply("35045020830", 77.88f),
                                    "45000000000",
                                    new CurrentPrice("0.248915", "0.231985", "0.0000095"),
                                    "29348197308",
                                    new PriceChange(
                                        new BigDecimal("7.00"),
                                        new BigDecimal("1.00"),
                                        new BigDecimal("-2.00")
                                    )
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
                                11,
                                new MarketData(
                                    new CirculatingSupply("140978466383", 0),
                                    "0",
                                    new CurrentPrice("0.061481", "0.057319", "0.00000235"),
                                    "11195832359",
                                    new PriceChange(
                                        new BigDecimal("-4.00"),
                                        new BigDecimal("-1.00"),
                                        new BigDecimal("-8.00")
                                    )
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
                                6,
                                new MarketData(
                                    new CirculatingSupply("53083046512", 53.08f),
                                    "100000000000",
                                    new CurrentPrice("0.478363", "0.446699", "0.00001833"),
                                    "29348197308",
                                    new PriceChange(
                                        new BigDecimal("2.00"),
                                        new BigDecimal("3.00"),
                                        new BigDecimal("-5.00")
                                    )
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

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(0, sortParams);

        assertTrue(userCryptosPlatformsInsights.isEmpty());
    }

    @Test
    void shouldRetrieveEmptyIfNoUserCryptosAreFoundForPageForRetrieveUserCryptosPlatformsInsights() {
        var cryptos = List.of("bitcoin", "ethereum", "tether");
        var userCryptos = userCryptos().stream().filter(userCrypto -> cryptos.contains(userCrypto.getCrypto().getId())).toList();
        var cryptosEntities = cryptos().stream().filter(crypto -> cryptos.contains(crypto.getId())).toList();
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        when(cryptoServiceMock.findAllByIds(Set.of("bitcoin", "tether", "ethereum"))).thenReturn(cryptosEntities);
        when(platformServiceMock.findAllByIds(Set.of("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "a76b400e-8ffc-42d6-bf47-db866eb20153")))
            .thenReturn(List.of(binancePlatform, coinbasePlatform));
        when(userCryptoServiceMock.findAll()).thenReturn(userCryptos);

        var userCryptosPlatformsInsights = insightsService.retrieveUserCryptosPlatformsInsights(1, sortParams);

        assertTrue(userCryptosPlatformsInsights.isEmpty());
    }

    private List<UserCrypto> userCryptos() {
        var binancePlatform = new Platform("163b1731-7a24-4e23-ac90-dc95ad8cb9e8", "BINANCE");
        var coinbasePlatform = new Platform("a76b400e-8ffc-42d6-bf47-db866eb20153", "COINBASE");

        return List.of(
            new UserCrypto(
                "676fb38a-556e-11ee-b56e-325096b39f47", new BigDecimal("0.15"), binancePlatform, getBitcoinCryptoEntity()
            ),
            new UserCrypto(
                "676fb600-556e-11ee-83b6-325096b39f47", new BigDecimal("200"), binancePlatform, getTetherCrypto()
            ),
            new UserCrypto(
                "676fb696-556e-11ee-aa1c-325096b39f47", new BigDecimal("0.26"), binancePlatform, getEthereumCrypto()
            ),
            new UserCrypto(
                "676fba74-556e-11ee-9bff-325096b39f47", new BigDecimal("1.112"), coinbasePlatform, getEthereumCrypto()
            ),
            new UserCrypto(
                "676fb70e-556e-11ee-8c2c-325096b39f47", new BigDecimal("3.125"), coinbasePlatform, getLitecoinCrypto()
            ),
            new UserCrypto(
                "676fb768-556e-11ee-8b42-325096b39f47", new BigDecimal("1"), binancePlatform, getBNBCrypto()
            ),
            new UserCrypto(
                "676fb7c2-556e-11ee-9800-325096b39f47", new BigDecimal("50"), coinbasePlatform, getXRPCrypto()
            ),
            new UserCrypto(
                "676fb83a-556e-11ee-9731-325096b39f47", new BigDecimal("150"), binancePlatform, getCardanoCrypto()
            ),
            new UserCrypto(
                "676fb89e-556e-11ee-b0b8-325096b39f47", new BigDecimal("40"), coinbasePlatform, getPolkadotCrypto()
            ),
            new UserCrypto(
                "676fb8e4-556e-11ee-883e-325096b39f47", new BigDecimal("10"), binancePlatform, getSolanaCrypto()
            ),
            new UserCrypto(
                "676fb92a-556e-11ee-9de1-325096b39f47", new BigDecimal("100"), coinbasePlatform, getPolygonCrypto()
            ),
            new UserCrypto(
                "676fb966-556e-11ee-81d6-325096b39f47", new BigDecimal("35"), binancePlatform, getChainlinkCrypto()
            ),
            new UserCrypto(
                "676fb9ac-556e-11ee-b4fa-325096b39f47", new BigDecimal("500"), coinbasePlatform, getDogecoinCrypto()
            ),
            new UserCrypto(
                "676fb9f2-556e-11ee-a929-325096b39f47", new BigDecimal("25"), binancePlatform, getAvalancheCrypto()
            ),
            new UserCrypto(
                "676fba2e-556e-11ee-a181-325096b39f47", new BigDecimal("30"), coinbasePlatform, getUniswapCrypto()
            )
        );
    }

    private List<Crypto> cryptos() {
        return List.of(
            getBitcoinCryptoEntity(),
            getTetherCrypto(),
            getEthereumCrypto(),
            getLitecoinCrypto(),
            getBNBCrypto(),
            getXRPCrypto(),
            getCardanoCrypto(),
            getPolkadotCrypto(),
            getSolanaCrypto(),
            getPolygonCrypto(),
            getChainlinkCrypto(),
            getDogecoinCrypto(),
            getAvalancheCrypto(),
            getUniswapCrypto()
        );
    }

    private Crypto getUniswapCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Uniswap",
            "uni",
            "https://assets.coingecko.com/coins/images/12504/large/uni.jpg?1687143398",
            22,
            new BigDecimal("4772322900"),
            new BigDecimal("753766667"),
            new BigDecimal("1000000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("4.25"),
            new BigDecimal("3.96"),
            new BigDecimal("0.00016197")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("2.00"),
            new BigDecimal("-1.00"),
            new BigDecimal("3.00")
        );

        return new Crypto("uniswap", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getAvalancheCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Avalanche",
            "avax",
            "https://assets.coingecko.com/coins/images/12559/large/Avalanche_Circle_RedWhite_Trans.png?1670992574",
            10,
            new BigDecimal("11953262327"),
            new BigDecimal("353804673"),
            new BigDecimal("720000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("9.3"),
            new BigDecimal("8.67"),
            new BigDecimal("0.00035516")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("4.00"),
            new BigDecimal("1.00"),
            new BigDecimal("8.00")
        );

        return new Crypto("avalanche-2", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getDogecoinCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Dogecoin",
            "doge",
            "https://assets.coingecko.com/coins/images/5/large/dogecoin.png?1547792256",
            11,
            new BigDecimal("11195832359"),
            new BigDecimal("140978466383"),
            BigDecimal.ZERO
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("0.061481"),
            new BigDecimal("0.057319"),
            new BigDecimal("0.00000235")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("-4.00"),
            new BigDecimal("-1.00"),
            new BigDecimal("-8.00")
        );

        return new Crypto("dogecoin", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getChainlinkCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Chainlink",
            "link",
            "https://assets.coingecko.com/coins/images/877/large/chainlink-new-logo.png?1547034700",
            14,
            new BigDecimal("9021587267"),
            new BigDecimal("538099971"),
            new BigDecimal("1000000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("5.99"),
            new BigDecimal("5.58"),
            new BigDecimal("0.00022866")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("4.00"),
            new BigDecimal("-1.00"),
            new BigDecimal("8.00")
        );

        return new Crypto("chainlink", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getPolygonCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Polygon",
            "matic",
            "https://assets.coingecko.com/coins/images/4713/large/matic-token-icon.png?1624446912",
            16,
            new BigDecimal("7001911961"),
            new BigDecimal("9319469069"),
            new BigDecimal("10000000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("0.509995"),
            new BigDecimal("0.475407"),
            new BigDecimal("0.00001947")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("14.00"),
            new BigDecimal("-10.00"),
            new BigDecimal("2.00")
        );

        return new Crypto("matic-network", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getSolanaCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Solana",
            "sol",
            "https://assets.coingecko.com/coins/images/4128/large/solana.png?1640133422",
            5,
            new BigDecimal("40090766907"),
            new BigDecimal("410905807"),
            BigDecimal.ZERO
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("18.04"),
            new BigDecimal("16.82"),
            new BigDecimal("0.00068809")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("4.00"),
            new BigDecimal("1.00"),
            new BigDecimal("-2.00")
        );

        return new Crypto("solana", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getPolkadotCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Polkadot",
            "dot",
            "https://assets.coingecko.com/coins/images/12171/large/polkadot.png?1639712644",
            13,
            new BigDecimal("8993575127"),
            new BigDecimal("1274258350"),
            BigDecimal.ZERO
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("4.01"),
            new BigDecimal("3.73"),
            new BigDecimal("0.00015302")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("4.00"),
            new BigDecimal("-1.00"),
            new BigDecimal("2.00")
        );

        return new Crypto("polkadot", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getCardanoCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Cardano",
            "ada",
            "https://assets.coingecko.com/coins/images/975/large/cardano.png?1547034860",
            9,
            new BigDecimal("29348197308"),
            new BigDecimal("35045020830"),
            new BigDecimal("45000000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("0.248915"),
            new BigDecimal("0.231985"),
            new BigDecimal("0.0000095")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("7.00"),
            new BigDecimal("1.00"),
            new BigDecimal("-2.00")
        );

        return new Crypto("cardano", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getXRPCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "XRP",
            "xrp",
            "https://assets.coingecko.com/coins/images/44/large/xrp-symbol-white-128.png?1605778731",
            6,
            new BigDecimal("29348197308"),
            new BigDecimal("53083046512"),
            new BigDecimal("100000000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("0.478363"),
            new BigDecimal("0.446699"),
            new BigDecimal("0.00001833")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("2.00"),
            new BigDecimal("3.00"),
            new BigDecimal("-5.00")
        );

        return new Crypto("ripple", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getBNBCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "BNB",
            "bnb",
            "https://assets.coingecko.com/coins/images/825/large/bnb-icon2_2x.png?1644979850",
            4,
            new BigDecimal("48318686968"),
            new BigDecimal("153856150"),
            new BigDecimal("200000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("211.79"),
            new BigDecimal("197.8"),
            new BigDecimal("0.00811016")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("6.00"),
            new BigDecimal("-2.00"),
            new BigDecimal("12.00")
        );

        return new Crypto("binancecoin", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getLitecoinCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Litecoin",
            "ltc",
            "https://assets.coingecko.com/coins/images/2/large/litecoin.png?1547033580",
            19,
            new BigDecimal("5259205267"),
            new BigDecimal("73638701"),
            new BigDecimal("84000000")
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("60.59"),
            new BigDecimal("56.56"),
            new BigDecimal("0.00235292")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("6.00"),
            new BigDecimal("-2.00"),
            new BigDecimal("12.00")
        );

        return new Crypto("litecoin", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getEthereumCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Ethereum",
            "eth",
            "https://assets.coingecko.com/coins/images/279/large/ethereum.png?1595348880",
            2,
            new BigDecimal("298219864117"),
            new BigDecimal("120220572"),
            BigDecimal.ZERO
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("1617.44"),
            new BigDecimal("1509.37"),
            new BigDecimal("0.06280356")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("10.00"),
            new BigDecimal("-5.00"),
            new BigDecimal("2.00")
        );

        return new Crypto("ethereum", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private Crypto getTetherCrypto() {
        var cryptoInfo = new com.distasilucas.cryptobalancetracker.entity.CryptoInfo(
            "Tether",
            "usdt",
            "https://assets.coingecko.com/coins/images/325/large/Tether.png?1668148663",
            3,
            new BigDecimal("95085861049"),
            new BigDecimal("83016246102"),
            BigDecimal.ZERO
        );
        var lastKnownPrices = new LastKnownPrices(
            new BigDecimal("0.999618"),
            new BigDecimal("0.933095"),
            new BigDecimal("0.0000388")
        );
        var changePercentages = new ChangePercentages(
            new BigDecimal("0.00"),
            new BigDecimal("0.00"),
            new BigDecimal("0.00")
        );

        return new Crypto("tether", cryptoInfo, lastKnownPrices, changePercentages, localDateTime);
    }

    private List<LocalDate> getMockDates(LocalDate now, int iteration, int daysSubtraction) {
        List<LocalDate> dates = new ArrayList<>();
        dates.add(now);

        for (int i = 1; i < iteration; i++) {
            dates.add(now.minusDays(daysSubtraction));
            now = now.minusDays(daysSubtraction);
        }

        return dates;
    }

    private List<LocalDate> getMockDates(LocalDate now) {
        List<LocalDate> dates = new ArrayList<>();
        dates.add(now);

        IntStream.range(1, 12)
            .forEach(n -> dates.add(now.minusMonths(n)));

        return dates;
    }

    private List<DateBalance> retrieveLastTwelveDaysBalances() {
        var now = LocalDate.of(2024, 3, 11);

        return List.of(
            new DateBalance("", now.minusDays(6), "1000"),
            new DateBalance("", now.minusDays(5), "850"),
            new DateBalance("", now.minusDays(4), "900"),
            new DateBalance("", now.minusDays(3), "1150"),
            new DateBalance("", now.minusDays(2), "1050"),
            new DateBalance("", now.minusDays(1), "1200"),
            new DateBalance("", now, "1150")
        );
    }

}
