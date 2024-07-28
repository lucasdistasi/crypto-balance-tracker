package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.BalanceType;
import com.distasilucas.cryptobalancetracker.model.DateRange;
import com.distasilucas.cryptobalancetracker.model.SortParams;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalanceChanges;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CirculatingSupply;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.DatesBalanceResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.DateBalances;
import com.distasilucas.cryptobalancetracker.model.response.insights.DifferencesChanges;
import com.distasilucas.cryptobalancetracker.model.response.insights.MarketData;
import com.distasilucas.cryptobalancetracker.model.response.insights.UserCryptosInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptoInsightResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.CryptosBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PageUserCryptosInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.crypto.PlatformInsight;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsBalancesInsightsResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.platform.PlatformsInsights;
import com.distasilucas.cryptobalancetracker.repository.DateBalanceRepository;
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTOS_BALANCES_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.CRYPTO_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.DATES_BALANCES_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORMS_BALANCES_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.PLATFORM_INSIGHTS_CACHE;
import static com.distasilucas.cryptobalancetracker.constants.Constants.TOTAL_BALANCES_CACHE;
import static java.lang.Math.ceil;

@Slf4j
@Service
public class InsightsService {

    private static final Double ELEMENTS_PER_PAGE = 10.0;
    private static final int INT_ELEMENTS_PER_PAGE = ELEMENTS_PER_PAGE.intValue();

    private final int max;
    private final PlatformService platformService;
    private final UserCryptoService userCryptoService;
    private final CryptoService cryptoService;
    private final DateBalanceRepository dateBalanceRepository;
    private final Clock clock;

    public InsightsService(@Value("${insights.cryptos}") int max,
                           PlatformService platformService,
                           UserCryptoService userCryptoService,
                           CryptoService cryptoService,
                           DateBalanceRepository dateBalanceRepository,
                           Clock clock) {
        this.max = max;
        this.platformService = platformService;
        this.userCryptoService = userCryptoService;
        this.cryptoService = cryptoService;
        this.dateBalanceRepository = dateBalanceRepository;
        this.clock = clock;
    }

    @Cacheable(cacheNames = TOTAL_BALANCES_CACHE)
    public BalancesResponse retrieveTotalBalancesInsights() {
        log.info("Retrieving total balances");

        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return BalancesResponse.empty();
        }

        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var cryptosIds = userCryptos.stream().map(userCrypto -> userCrypto.getCrypto().getId()).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);

        return getTotalBalances(cryptos, userCryptoQuantity);
    }

    @Cacheable(cacheNames = DATES_BALANCES_CACHE, key = "#dateRange")
    public DatesBalanceResponse retrieveDatesBalances(DateRange dateRange) {
        log.info("Retrieving balances for date range: {}", dateRange);
        List<DateBalance> dateBalances = new ArrayList<>();
        var now = LocalDateTime.now(clock).toLocalDate();

        switch (dateRange) {
            case ONE_DAY -> dateBalances.addAll(retrieveDatesBalances(now.minusDays(2), now));
            case THREE_DAYS -> dateBalances.addAll(retrieveDatesBalances(now.minusDays(3), now));
            case ONE_WEEK -> dateBalances.addAll(retrieveDatesBalances(now.minusWeeks(1), now));
            case ONE_MONTH -> dateBalances.addAll(retrieveDatesBalances(2, 4, now.minusMonths(1), now));
            case THREE_MONTHS -> dateBalances.addAll(retrieveDatesBalances(6, 5, now.minusMonths(3), now));
            case SIX_MONTHS -> dateBalances.addAll(retrieveDatesBalances(10, 6, now.minusMonths(6), now));
            case ONE_YEAR -> dateBalances.addAll(retrieveYearDatesBalances(now));
        }

        var datesBalances = dateBalances
            .stream()
            .map(dateBalance -> {
                String formattedDate = dateBalance.getDate().format(DateTimeFormatter.ofPattern("d MMMM yyyy"));
                var balancesResponse = new BalancesResponse(dateBalance.getBalances());
                return new DateBalances(formattedDate, balancesResponse);
            })
            .toList();
        log.info("Balances found: {}", datesBalances.size());

        if (datesBalances.isEmpty()) {
            return DatesBalanceResponse.empty();
        }

        var changesPair = changesPair(datesBalances);

        return new DatesBalanceResponse(datesBalances, changesPair.getFirst(), changesPair.getSecond());
    }

    @Cacheable(cacheNames = PLATFORM_INSIGHTS_CACHE, key = "#platformId")
    public PlatformInsightsResponse retrievePlatformInsights(String platformId) {
        log.info("Retrieving insights for platform with id {}", platformId);

        var userCryptosInPlatform = userCryptoService.findAllByPlatformId(platformId);

        if (userCryptosInPlatform.isEmpty()) {
            return PlatformInsightsResponse.empty();
        }

        var platformResponse = platformService.retrievePlatformById(platformId);
        var cryptosIds = userCryptosInPlatform.stream().map(userCrypto -> userCrypto.getCrypto().getId()).toList();
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var userCryptosQuantity = getUserCryptoQuantity(userCryptosInPlatform);
        var totalBalances = getTotalBalances(cryptos, userCryptosQuantity);

        var cryptosInsights = userCryptosInPlatform.stream()
            .map(userCrypto -> {
                var quantity = userCryptosQuantity.get(userCrypto.getCrypto().getId());
                var crypto = cryptos.stream()
                    .filter(c -> userCrypto.getCrypto().getId().equals(c.getId()))
                    .findFirst()
                    .get();
                var cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity);

                return new CryptoInsights(
                    userCrypto.getId(),
                    crypto.getCryptoInfo().getName(),
                    crypto.getId(),
                    quantity.toPlainString(),
                    cryptoTotalBalances,
                    calculatePercentage(totalBalances.totalUSDBalance(), cryptoTotalBalances.totalUSDBalance())
                );
            })
            .sorted(Comparator.comparing(CryptoInsights::percentage, Comparator.reverseOrder()))
            .toList();

        return new PlatformInsightsResponse(platformResponse.getName(), totalBalances, cryptosInsights);
    }

    @Cacheable(cacheNames = CRYPTO_INSIGHTS_CACHE, key = "#coingeckoCryptoId")
    public CryptoInsightResponse retrieveCryptoInsights(String coingeckoCryptoId) {
        log.info("Retrieving insights for crypto with coingeckoCryptoId {}", coingeckoCryptoId);

        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId);

        if (userCryptos.isEmpty()) {
            return CryptoInsightResponse.empty();
        }

        var platformsIds = userCryptos.stream().map(userCrypto -> userCrypto.getPlatform().getId()).toList();
        var platforms = platformService.findAllByIds(platformsIds);
        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCryptoId);

        var platformUserCryptoQuantity = userCryptos.stream()
            .collect(Collectors.toMap(userCrypto -> userCrypto.getPlatform().getId(), UserCrypto::getQuantity));
        var totalCryptoQuantity = userCryptos.stream()
            .map(UserCrypto::getQuantity)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalBalances = getTotalBalances(List.of(crypto), Map.of(coingeckoCryptoId, totalCryptoQuantity));

        var platformInsights = platforms.stream()
            .map(platform -> {
                var quantity = platformUserCryptoQuantity.get(platform.getId());
                var cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity);

                return new PlatformInsight(
                    quantity.toPlainString(),
                    cryptoTotalBalances,
                    calculatePercentage(totalBalances.totalUSDBalance(), cryptoTotalBalances.totalUSDBalance()),
                    platform.getName()
                );
            })
            .sorted(Comparator.comparing(PlatformInsight::percentage, Comparator.reverseOrder()))
            .toList();

        return new CryptoInsightResponse(crypto.getCryptoInfo().getName(), totalBalances, platformInsights);
    }

    @Cacheable(cacheNames = PLATFORMS_BALANCES_INSIGHTS_CACHE)
    public PlatformsBalancesInsightsResponse retrievePlatformsBalancesInsights() {
        log.info("Retrieving all platforms balances insights");

        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return PlatformsBalancesInsightsResponse.empty();
        }

        var platformsIds = userCryptos.stream()
            .map(userCrypto -> userCrypto.getPlatform().getId())
            .collect(Collectors.toSet());
        var platforms = platformService.findAllByIds(platformsIds);
        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var platformsUserCryptos = getPlatformsUserCryptos(userCryptos, platforms);
        var cryptosIds = platformsUserCryptos.values()
            .stream()
            .flatMap(cryptos -> cryptos.stream().map(userCrypto -> userCrypto.getCrypto().getId()))
            .collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);

        var platformsInsights = platformsUserCryptos.entrySet()
            .stream()
            .map(entry -> {
                var platformName = entry.getKey();
                var cryptosUser = entry.getValue();

                var totalUSDBalance = BigDecimal.ZERO;
                var totalBTCBalance = BigDecimal.ZERO;
                var totalEURBalance = BigDecimal.ZERO;

                for (var userCrypto : cryptosUser) {
                    var crypto = cryptos.stream()
                        .filter(c -> c.getId().equalsIgnoreCase(userCrypto.getCrypto().getId()))
                        .findFirst()
                        .orElseThrow();
                    var balance = getCryptoTotalBalances(crypto, userCrypto.getQuantity());

                    totalUSDBalance = totalUSDBalance.add(new BigDecimal(balance.totalUSDBalance()));
                    totalBTCBalance = totalBTCBalance.add(new BigDecimal(balance.totalBTCBalance()));
                    totalEURBalance = totalEURBalance.add(new BigDecimal(balance.totalEURBalance()));
                }

                var balances = new BalancesResponse(
                    totalUSDBalance.toPlainString(),
                    totalEURBalance.toPlainString(),
                    totalBTCBalance.toPlainString()
                );
                var percentage = calculatePercentage(totalBalances.totalUSDBalance(), totalUSDBalance.toPlainString());

                return new PlatformsInsights(platformName, balances, percentage);
            })
            .sorted(Comparator.comparing(PlatformsInsights::percentage, Comparator.reverseOrder()))
            .toList();

        return new PlatformsBalancesInsightsResponse(totalBalances, platformsInsights);
    }

    @Cacheable(cacheNames = CRYPTOS_BALANCES_INSIGHTS_CACHE)
    public CryptosBalancesInsightsResponse retrieveCryptosBalancesInsights() {
        log.info("Retrieving all cryptos balances insights");

        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return CryptosBalancesInsightsResponse.empty();
        }

        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var cryptosIds = userCryptos.stream().map(userCrypto -> userCrypto.getCrypto().getId()).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);

        var cryptosInsights = userCryptoQuantity.entrySet()
            .stream()
            .map(entry -> {
                var coingeckoCryptoId = entry.getKey();
                var quantity = entry.getValue();
                var crypto = cryptos.stream()
                    .filter(c -> c.getId().equalsIgnoreCase(coingeckoCryptoId))
                    .findFirst()
                    .orElseThrow();
                var cryptoBalances = getCryptoTotalBalances(crypto, quantity);

                return new CryptoInsights(
                    crypto.getCryptoInfo().getName(),
                    coingeckoCryptoId,
                    quantity.toPlainString(),
                    cryptoBalances,
                    calculatePercentage(totalBalances.totalUSDBalance(), cryptoBalances.totalUSDBalance())
                );
            })
            .sorted(Comparator.comparing(CryptoInsights::percentage, Comparator.reverseOrder()))
            .toList();

        var cryptosToReturn = cryptosInsights.size() > max ?
            getCryptoInsightsWithOthers(totalBalances, cryptosInsights) :
            cryptosInsights;

        return new CryptosBalancesInsightsResponse(totalBalances, cryptosToReturn);
    }

    public Optional<PageUserCryptosInsightsResponse> retrieveUserCryptosInsights(int page, SortParams sortParams) {
        log.info("Retrieving user cryptos insights for page {} with sort params {}", page, sortParams);

        // Not the best because I'm paginating, but I need total balances to calculate individual percentages
        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var cryptosIds = userCryptos.stream().map(userCrypto -> userCrypto.getCrypto().getId()).collect(Collectors.toSet());
        var platformsIds = userCryptos.stream()
            .map(userCrypto -> userCrypto.getPlatform().getId())
            .collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var platforms = platformService.findAllByIds(platformsIds);
        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);

        List<UserCryptosInsights> userCryptosInsights = new ArrayList<>();

        for (var userCrypto : userCryptos) {
            var crypto = cryptos.stream()
                .filter(c -> c.getId().equalsIgnoreCase(userCrypto.getCrypto().getId()))
                .findFirst()
                .orElseThrow();
            var platform = platforms.stream()
                .filter(p -> p.getId().equalsIgnoreCase(userCrypto.getPlatform().getId()))
                .findFirst()
                .orElseThrow();
            var balances = getCryptoTotalBalances(crypto, userCrypto.getQuantity());
            var circulatingSupply = getCirculatingSupply(crypto.getCryptoInfo().getMaxSupply(), crypto.getCryptoInfo().getCirculatingSupply());

            var userCryptosInsight = new UserCryptosInsights(
                userCrypto,
                crypto,
                calculatePercentage(totalBalances.totalUSDBalance(), balances.totalUSDBalance()),
                balances,
                new MarketData(circulatingSupply, crypto),
                List.of(platform.getName())
            );

            userCryptosInsights.add(userCryptosInsight);
        }

        userCryptosInsights = userCryptosInsights.stream()
            .sorted(sortParams.cryptosInsightsResponseComparator())
            .toList();

        var startIndex = page * INT_ELEMENTS_PER_PAGE;

        if (startIndex > userCryptosInsights.size()) {
            return Optional.empty();
        }

        var totalPages = (int) ceil(userCryptos.size() / ELEMENTS_PER_PAGE);
        var endIndex = isLastPage(page, totalPages) ? userCryptosInsights.size() : startIndex + INT_ELEMENTS_PER_PAGE;
        var cryptosInsights = userCryptosInsights.subList(startIndex, endIndex);

        return Optional.of(new PageUserCryptosInsightsResponse(page, totalPages, totalBalances, cryptosInsights));
    }

    public Optional<PageUserCryptosInsightsResponse> retrieveUserCryptosPlatformsInsights(int page, SortParams sortParams) {
        log.info("Retrieving user cryptos in platforms insights for page {} with sort params {}", page, sortParams);

        // If one of the user cryptos happens to be at the end, and another of the same (i.e: bitcoin), at the start
        // using findAllByPage() will display the same crypto twice (in this example), and the idea of this insight
        // it's to display total balances and percentage for each individual crypto.
        // So I need to calculate everything from all the user cryptos.
        // Maybe create a query that returns the coingeckoCryptoId summing all balances for that crypto and
        // returning an array of the platforms for that crypto and then paginate the results
        // would be a better approach, so I don't need to retrieve all.
        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var cryptosIds = userCryptos.stream().map(userCrypto -> userCrypto.getCrypto().getId()).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var platformsIds = userCryptos.stream()
            .map(userCrypto -> userCrypto.getPlatform().getId())
            .collect(Collectors.toSet());
        var platforms = platformService.findAllByIds(platformsIds);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);
        var userCryptosQuantityPlatforms = getUserCryptosQuantityPlatforms(userCryptos, platforms);

        var userCryptosInsights = userCryptosQuantityPlatforms.entrySet()
            .stream()
            .map(entry -> {
                var cryptoTotalQuantity = entry.getValue().component1();
                var cryptoPlatforms = entry.getValue().component2();
                var crypto = cryptos.stream()
                    .filter(c -> c.getId().equalsIgnoreCase(entry.getKey()))
                    .findFirst()
                    .orElseThrow();
                var cryptoTotalBalances = getCryptoTotalBalances(crypto, cryptoTotalQuantity);
                var circulatingSupply = getCirculatingSupply(crypto.getCryptoInfo().getMaxSupply(), crypto.getCryptoInfo().getCirculatingSupply());

                return new UserCryptosInsights(
                    new CryptoInfo(crypto.getCryptoInfo().getName(), crypto.getId(), crypto.getCryptoInfo().getTicker(), crypto.getCryptoInfo().getImage()),
                    cryptoTotalQuantity.toPlainString(),
                    calculatePercentage(totalBalances.totalUSDBalance(), cryptoTotalBalances.totalUSDBalance()),
                    cryptoTotalBalances,
                    crypto.getCryptoInfo().getMarketCapRank(),
                    new MarketData(circulatingSupply, crypto),
                    cryptoPlatforms
                );
            })
            .sorted(sortParams.cryptosInsightsResponseComparator())
            .toList();

        var startIndex = page * INT_ELEMENTS_PER_PAGE;

        if (startIndex > userCryptosInsights.size()) {
            return Optional.empty();
        }

        var totalPages = (int) ceil(userCryptosInsights.size() / ELEMENTS_PER_PAGE);
        var endIndex = isLastPage(page, totalPages) ? userCryptosInsights.size() : startIndex + INT_ELEMENTS_PER_PAGE;
        var cryptosInsights = userCryptosInsights.subList(startIndex, endIndex);

        return Optional.of(new PageUserCryptosInsightsResponse(page, totalPages, totalBalances, cryptosInsights));
    }

    private Map<String, BigDecimal> getUserCryptoQuantity(List<UserCrypto> userCryptos) {
        var userCryptoQuantity = new HashMap<String, BigDecimal>();

        userCryptos.forEach(userCrypto -> {
            if (userCryptoQuantity.containsKey(userCrypto.getCrypto().getId())) {
                var quantity = userCryptoQuantity.get(userCrypto.getCrypto().getId());
                userCryptoQuantity.put(userCrypto.getCrypto().getId(), quantity.add(userCrypto.getQuantity()));
            } else {
                userCryptoQuantity.put(userCrypto.getCrypto().getId(), userCrypto.getQuantity());
            }
        });

        return userCryptoQuantity;
    }

    private BalancesResponse getTotalBalances(List<Crypto> cryptos, Map<String, BigDecimal> userCryptoQuantity) {
        var totalUSDBalance = BigDecimal.ZERO;
        var totalBTCBalance = BigDecimal.ZERO;
        var totalEURBalance = BigDecimal.ZERO;

        var entries = userCryptoQuantity.entrySet();
        for (Map.Entry<String, BigDecimal> entry : entries) {
            var coingeckoCryptoId = entry.getKey();
            var quantity = entry.getValue();

            var crypto = cryptos.stream()
                .filter(c -> c.getId().equalsIgnoreCase(coingeckoCryptoId))
                .findFirst()
                .orElseThrow();
            var lastKnownPrice = crypto.getLastKnownPrices().getLastKnownPrice();
            var lastKnownPriceInBTC = crypto.getLastKnownPrices().getLastKnownPriceInBTC();
            var lastKnownPriceInEUR = crypto.getLastKnownPrices().getLastKnownPriceInEUR();

            totalUSDBalance = totalUSDBalance.add(lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP));
            totalBTCBalance = totalBTCBalance.add(lastKnownPriceInBTC.multiply(quantity)).stripTrailingZeros();
            totalEURBalance = totalEURBalance.add(lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP));
        }

        return new BalancesResponse(
            totalUSDBalance.toPlainString(),
            totalEURBalance.toPlainString(),
            totalBTCBalance.setScale(10, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
        );
    }

    private BalancesResponse getCryptoTotalBalances(Crypto crypto, BigDecimal quantity) {
        return new BalancesResponse(
            crypto.getLastKnownPrices().getLastKnownPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
            crypto.getLastKnownPrices().getLastKnownPriceInEUR().multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
            crypto.getLastKnownPrices().getLastKnownPriceInBTC().multiply(quantity).setScale(10, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
        );
    }

    private CirculatingSupply getCirculatingSupply(BigDecimal maxSupply, BigDecimal circulatingSupply) {
        var circulatingSupplyPercentage = 0f;

        if (BigDecimal.ZERO.compareTo(maxSupply) < 0) {
            circulatingSupplyPercentage = circulatingSupply.multiply(new BigDecimal("100"))
                .divide(maxSupply, 2, RoundingMode.HALF_UP)
                .floatValue();
        }

        return new CirculatingSupply(circulatingSupply.toPlainString(), circulatingSupplyPercentage);
    }

    private float calculatePercentage(String totalUSDBalance, String cryptoBalance) {
        return new BigDecimal(cryptoBalance)
            .multiply(new BigDecimal("100"))
            .divide(new BigDecimal(totalUSDBalance), 2, RoundingMode.HALF_UP)
            .floatValue();
    }

    private Map<String, List<UserCrypto>> getPlatformsUserCryptos(List<UserCrypto> userCryptos, List<Platform> platforms) {
        var platformsUserCryptos = new HashMap<String, List<UserCrypto>>();

        userCryptos.forEach(userCrypto -> {
            var platform = platforms.stream()
                .filter(p -> p.getId().equalsIgnoreCase(userCrypto.getPlatform().getId()))
                .findFirst()
                .orElseThrow();

            if (platformsUserCryptos.containsKey(platform.getName())) {
                var cryptos = new ArrayList<>(platformsUserCryptos.get(platform.getName()));
                cryptos.add(userCrypto);
                platformsUserCryptos.put(platform.getName(), cryptos);
            } else {
                platformsUserCryptos.put(platform.getName(), List.of(userCrypto));
            }
        });

        return platformsUserCryptos;
    }

    private List<CryptoInsights> getCryptoInsightsWithOthers(BalancesResponse totalBalances, List<CryptoInsights> cryptosInsights) {
        var topCryptos = cryptosInsights.subList(0, max);
        var others = cryptosInsights.subList(max, cryptosInsights.size());

        var totalUSDBalance = BigDecimal.ZERO;
        var totalBTCBalance = BigDecimal.ZERO;
        var totalEURBalance = BigDecimal.ZERO;

        for (var cryptoInsights : others) {
            totalUSDBalance = totalUSDBalance.add(new BigDecimal(cryptoInsights.balances().totalUSDBalance()));
            totalBTCBalance = totalBTCBalance.add(new BigDecimal(cryptoInsights.balances().totalBTCBalance()));
            totalEURBalance = totalEURBalance.add(new BigDecimal(cryptoInsights.balances().totalEURBalance()));
        }

        var othersTotalPercentage = calculatePercentage(totalBalances.totalUSDBalance(), totalUSDBalance.toPlainString());
        var balancesResponse = new BalancesResponse(
            totalUSDBalance.toPlainString(),
            totalEURBalance.toPlainString(),
            totalBTCBalance.toPlainString()
        );
        var othersCryptoInsights = new CryptoInsights("Others", balancesResponse, othersTotalPercentage);

        List<CryptoInsights> cryptosInsightsWithOthers = new ArrayList<>(topCryptos);
        cryptosInsightsWithOthers.add(othersCryptoInsights);

        return cryptosInsightsWithOthers;
    }

    private Map<String, Pair<BigDecimal, List<String>>> getUserCryptosQuantityPlatforms(
        List<UserCrypto> userCryptos,
        List<Platform> platforms
    ) {
        var map = new HashMap<String, Pair<BigDecimal, List<String>>>();

        userCryptos.forEach(userCrypto -> {
            var platformName = platforms.stream()
                .filter(p -> p.getId().equalsIgnoreCase(userCrypto.getPlatform().getId()))
                .findFirst()
                .orElseThrow()
                .getName();

            if (map.containsKey(userCrypto.getCrypto().getId())) {
                var crypto = map.get(userCrypto.getCrypto().getId());
                var actualQuantity = crypto.component1();
                var actualPlatforms = new ArrayList<>(crypto.component2());

                var newQuantity = actualQuantity.add(userCrypto.getQuantity());
                actualPlatforms.add(platformName);

                map.put(userCrypto.getCrypto().getId(), new Pair<>(newQuantity, actualPlatforms));
            } else {
                map.put(userCrypto.getCrypto().getId(), new Pair<>(userCrypto.getQuantity(), List.of(platformName)));
            }
        });

        return map;
    }

    private boolean isLastPage(int page, int totalPages) {
        return page + 1 >= totalPages;
    }

    private List<DateBalance> retrieveDatesBalances(LocalDate from, LocalDate to) {
        log.info("Retrieving date balances from {} to {}", from, to);

        return dateBalanceRepository.findDateBalancesByDateBetween(from, to);
    }

    private List<DateBalance> retrieveDatesBalances(long daysSubtraction, int minRequired,
                                                    LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();

        while (from.isBefore(to)) {
            dates.add(to);
            to = to.minusDays(daysSubtraction);
        }

        log.info("Searching balances for dates {}", dates);

        var datesBalances = dateBalanceRepository.findAllByDateIn(dates);
        log.info("Found dates balances {}", datesBalances.stream().map(DateBalance::getDate).toList());

        return datesBalances.size() >= minRequired ?
            datesBalances :
            retrieveLastTwelveDaysBalances();
    }

    private List<DateBalance> retrieveYearDatesBalances(LocalDate now) {
        List<LocalDate> dates = new ArrayList<>();
        dates.add(now);

        IntStream.range(1, 12)
            .forEach(n -> dates.add(now.minusMonths(n)));

        log.info("Searching balances for dates {}", dates);

        var datesBalances = dateBalanceRepository.findAllByDateIn(dates);

        return datesBalances.size() > 3 ?
            datesBalances :
            retrieveLastTwelveDaysBalances();
    }

    private List<DateBalance> retrieveLastTwelveDaysBalances() {
        var to = LocalDateTime.now(clock).toLocalDate();
        var from = to.minusDays(12).atTime(23, 59, 59, 0).toLocalDate();

        log.info("Not enough balances. Retrieving balances for the last twelve days from {} to {}", from, to);
        return dateBalanceRepository.findDateBalancesByDateBetween(from, to);
    }

    private Pair<BalanceChanges, DifferencesChanges> changesPair(List<DateBalances> dateBalances) {
        var usdChange = getChange(BalanceType.USD_BALANCE, dateBalances);
        var eurChange = getChange(BalanceType.EUR_BALANCE, dateBalances);
        var btcChange = getChange(BalanceType.BTC_BALANCE, dateBalances);

        return new Pair<>(
            new BalanceChanges(usdChange.getFirst(), eurChange.getFirst(), btcChange.getFirst()),
            new DifferencesChanges(usdChange.getSecond(), eurChange.getSecond(), btcChange.getSecond())
        );
    }

    private Pair<Float, String> getChange(BalanceType balanceType, List<DateBalances> dateBalances) {
        var newestValues = dateBalances.getLast().balances();
        var oldestValues = dateBalances.getFirst().balances();
        var divisionScale = 4;
        if (BalanceType.BTC_BALANCE == balanceType) divisionScale = 10;

        var values = switch (balanceType) {
            case USD_BALANCE -> new Pair<>(new BigDecimal(oldestValues.totalUSDBalance()), new BigDecimal(newestValues.totalUSDBalance()));
            case EUR_BALANCE -> new Pair<>(new BigDecimal(oldestValues.totalEURBalance()), new BigDecimal(newestValues.totalEURBalance()));
            case BTC_BALANCE -> new Pair<>(new BigDecimal(oldestValues.totalBTCBalance()), new BigDecimal(newestValues.totalBTCBalance()));
        };

        var newestValue = values.getSecond();
        var oldestValue = values.getFirst();

        var change = newestValue
            .subtract(oldestValue)
            .divide(oldestValue, divisionScale, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"))
            .setScale(2, RoundingMode.HALF_UP)
            .floatValue();
        var difference = newestValue.subtract(oldestValue).toPlainString();

        return new Pair<>(change, difference);
    }
}
