package com.distasilucas.cryptobalancetracker.service;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.Platform;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;
import com.distasilucas.cryptobalancetracker.model.SortParams;
import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import com.distasilucas.cryptobalancetracker.model.response.insights.CirculatingSupply;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInfo;
import com.distasilucas.cryptobalancetracker.model.response.insights.CryptoInsights;
import com.distasilucas.cryptobalancetracker.model.response.insights.CurrentPrice;
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
import kotlin.Pair;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public InsightsService(@Value("${insights.cryptos}") int max,
                           PlatformService platformService,
                           UserCryptoService userCryptoService,
                           CryptoService cryptoService) {
        this.max = max;
        this.platformService = platformService;
        this.userCryptoService = userCryptoService;
        this.cryptoService = cryptoService;
    }

    public Optional<BalancesResponse> retrieveTotalBalancesInsights() {
        log.info("Retrieving total balances");

        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var cryptosIds = userCryptos.stream().map(UserCrypto::coingeckoCryptoId).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);

        return Optional.of(totalBalances);
    }

    public Optional<PlatformInsightsResponse> retrievePlatformInsights(String platformId) {
        log.info("Retrieving insights for platform with id {}", platformId);

        var userCryptosInPlatform = userCryptoService.findAllByPlatformId(platformId);

        if (userCryptosInPlatform.isEmpty()) {
            return Optional.empty();
        }

        var platformResponse = platformService.retrievePlatformById(platformId);
        var cryptosIds = userCryptosInPlatform.stream().map(UserCrypto::coingeckoCryptoId).toList();
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var userCryptosQuantity = getUserCryptoQuantity(userCryptosInPlatform);
        var totalBalances = getTotalBalances(cryptos, userCryptosQuantity);

        var cryptosInsights = userCryptosInPlatform.stream()
                .map(userCrypto -> {
                    var quantity = userCryptosQuantity.get(userCrypto.coingeckoCryptoId());
                    var crypto = cryptos.stream()
                            .filter(c -> userCrypto.coingeckoCryptoId().equals(c.id()))
                            .findFirst()
                            .get();
                    var cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity);

                    return new CryptoInsights(
                            userCrypto.id(),
                            crypto.name(),
                            crypto.id(),
                            quantity.toPlainString(),
                            cryptoTotalBalances,
                            calculatePercentage(totalBalances.totalUSDBalance(), cryptoTotalBalances.totalUSDBalance())
                    );
                })
                .sorted(Comparator.comparing(CryptoInsights::percentage, Comparator.reverseOrder()))
                .toList();

        return Optional.of(new PlatformInsightsResponse(platformResponse.name(), totalBalances, cryptosInsights));
    }

    public Optional<CryptoInsightResponse> retrieveCryptoInsights(String coingeckoCryptoId) {
        log.info("Retrieving insights for crypto with coingeckoCryptoId {}", coingeckoCryptoId);

        var userCryptos = userCryptoService.findAllByCoingeckoCryptoId(coingeckoCryptoId);

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var platformsIds = userCryptos.stream().map(UserCrypto::platformId).toList();
        var platforms = platformService.findAllByIds(platformsIds);
        var crypto = cryptoService.retrieveCryptoInfoById(coingeckoCryptoId);

        var platformUserCryptoQuantity = userCryptos.stream().collect(Collectors.toMap(UserCrypto::platformId, UserCrypto::quantity));
        var totalCryptoQuantity = userCryptos.stream()
                .map(UserCrypto::quantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        var totalBalances = getTotalBalances(List.of(crypto), Map.of(coingeckoCryptoId, totalCryptoQuantity));

        var platformInsights = platforms.stream()
                .map(platform -> {
                    var quantity = platformUserCryptoQuantity.get(platform.id());
                    var cryptoTotalBalances = getCryptoTotalBalances(crypto, quantity);

                    return new PlatformInsight(
                            quantity.toPlainString(),
                            cryptoTotalBalances,
                            calculatePercentage(totalBalances.totalUSDBalance(), cryptoTotalBalances.totalUSDBalance()),
                            platform.name()
                    );
                })
                .sorted(Comparator.comparing(PlatformInsight::percentage, Comparator.reverseOrder()))
                .toList();

        return Optional.of(new CryptoInsightResponse(crypto.name(), totalBalances, platformInsights));
    }

    public Optional<PlatformsBalancesInsightsResponse> retrievePlatformsBalancesInsights() {
        log.info("Retrieving all platforms balances insights");

        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var platformsIds = userCryptos.stream().map(UserCrypto::platformId).collect(Collectors.toSet());
        var platforms = platformService.findAllByIds(platformsIds);
        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var platformsUserCryptos = getPlatformsUserCryptos(userCryptos, platforms);
        var cryptosIds = platformsUserCryptos.values()
                .stream()
                .flatMap(cryptos -> cryptos.stream().map(UserCrypto::coingeckoCryptoId))
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
                                .filter(c -> c.id().equalsIgnoreCase(userCrypto.coingeckoCryptoId()))
                                .findFirst()
                                .orElseThrow();
                        var balance = getCryptoTotalBalances(crypto, userCrypto.quantity());

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

        return Optional.of(new PlatformsBalancesInsightsResponse(totalBalances, platformsInsights));
    }

    public Optional<CryptosBalancesInsightsResponse> retrieveCryptosBalancesInsights() {
        log.info("Retrieving all cryptos balances insights");

        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var cryptosIds = userCryptos.stream().map(UserCrypto::coingeckoCryptoId).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);

        var cryptosInsights = userCryptoQuantity.entrySet()
                .stream()
                .map(entry -> {
                    var coingeckoCryptoId = entry.getKey();
                    var quantity = entry.getValue();
                    var crypto = cryptos.stream()
                            .filter(c -> c.id().equalsIgnoreCase(coingeckoCryptoId))
                            .findFirst()
                            .orElseThrow();
                    var cryptoBalances = getCryptoTotalBalances(crypto, quantity);

                    return new CryptoInsights(
                            crypto.name(),
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

        return Optional.of(new CryptosBalancesInsightsResponse(totalBalances, cryptosToReturn));
    }

    public Optional<PageUserCryptosInsightsResponse> retrieveUserCryptosInsights(int page, SortParams sortParams) {
        log.info("Retrieving user cryptos insights for page {} with sort params {}", page, sortParams);

        // Not the best because I'm paginating, but I need total balances to calculate individual percentages
        var userCryptos = userCryptoService.findAll();

        if (userCryptos.isEmpty()) {
            return Optional.empty();
        }

        var cryptosIds = userCryptos.stream().map(UserCrypto::coingeckoCryptoId).collect(Collectors.toSet());
        var platformsIds = userCryptos.stream().map(UserCrypto::platformId).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var platforms = platformService.findAllByIds(platformsIds);
        var userCryptoQuantity = getUserCryptoQuantity(userCryptos);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);

        List<UserCryptosInsights> userCryptosInsights = new ArrayList<>();

        for (var userCrypto : userCryptos) {
            var crypto = cryptos.stream()
                    .filter(c -> c.id().equalsIgnoreCase(userCrypto.coingeckoCryptoId()))
                    .findFirst()
                    .orElseThrow();
            var platform = platforms.stream()
                    .filter(p -> p.id().equalsIgnoreCase(userCrypto.platformId()))
                    .findFirst()
                    .orElseThrow();
            var balances = getCryptoTotalBalances(crypto, userCrypto.quantity());
            var circulatingSupply = getCirculatingSupply(crypto.maxSupply(), crypto.circulatingSupply());

            var userCryptosInsight = new UserCryptosInsights(
                    new CryptoInfo(
                            userCrypto.id(),
                            crypto.name(),
                            crypto.id(),
                            crypto.ticker(),
                            crypto.image()
                    ),
                    userCrypto.quantity().toPlainString(),
                    calculatePercentage(totalBalances.totalUSDBalance(), balances.totalUSDBalance()),
                    balances,
                    crypto.marketCapRank(),
                    new MarketData(
                            circulatingSupply,
                            crypto.maxSupply().toPlainString(),
                            new CurrentPrice(
                                    crypto.lastKnownPrice().toPlainString(),
                                    crypto.lastKnownPriceInEUR().toPlainString(),
                                    crypto.lastKnownPriceInBTC().toPlainString()
                            ),
                            crypto.marketCap().toPlainString(),
                            new PriceChange(
                                    crypto.changePercentageIn24h(),
                                    crypto.changePercentageIn7d(),
                                    crypto.changePercentageIn30d()
                            )
                    ),
                    List.of(platform.name())
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
        var cryptosIds = userCryptos.stream().map(UserCrypto::coingeckoCryptoId).collect(Collectors.toSet());
        var cryptos = cryptoService.findAllByIds(cryptosIds);
        var platformsIds = userCryptos.stream().map(UserCrypto::platformId).collect(Collectors.toSet());
        var platforms = platformService.findAllByIds(platformsIds);
        var totalBalances = getTotalBalances(cryptos, userCryptoQuantity);
        var userCryptosQuantityPlatforms = getUserCryptosQuantityPlatforms(userCryptos, platforms);

        var userCryptosInsights = userCryptosQuantityPlatforms.entrySet()
                .stream()
                .map(entry -> {
                    var cryptoTotalQuantity = entry.getValue().component1();
                    var cryptoPlatforms = entry.getValue().component2();
                    var crypto = cryptos.stream()
                            .filter(c -> c.id().equalsIgnoreCase(entry.getKey()))
                            .findFirst()
                            .orElseThrow();
                    var cryptoTotalBalances = getCryptoTotalBalances(crypto, cryptoTotalQuantity);
                    var circulatingSupply = getCirculatingSupply(crypto.maxSupply(), crypto.circulatingSupply());

                    return new UserCryptosInsights(
                            new CryptoInfo(crypto.name(), crypto.id(), crypto.ticker(), crypto.image()),
                            cryptoTotalQuantity.toPlainString(),
                            calculatePercentage(totalBalances.totalUSDBalance(), cryptoTotalBalances.totalUSDBalance()),
                            cryptoTotalBalances,
                            crypto.marketCapRank(),
                            new MarketData(
                                    circulatingSupply,
                                    crypto.maxSupply().toPlainString(),
                                    new CurrentPrice(
                                            crypto.lastKnownPrice().toPlainString(),
                                            crypto.lastKnownPriceInEUR().toPlainString(),
                                            crypto.lastKnownPriceInBTC().toPlainString()
                                    ),
                                    crypto.marketCap().toPlainString(),
                                    new PriceChange(
                                            crypto.changePercentageIn24h(),
                                            crypto.changePercentageIn7d(),
                                            crypto.changePercentageIn30d()
                                    )
                            ),
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
            if (userCryptoQuantity.containsKey(userCrypto.coingeckoCryptoId())) {
                var quantity = userCryptoQuantity.get(userCrypto.coingeckoCryptoId());
                userCryptoQuantity.put(userCrypto.coingeckoCryptoId(), quantity.add(userCrypto.quantity()));
            } else {
                userCryptoQuantity.put(userCrypto.coingeckoCryptoId(), userCrypto.quantity());
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
                    .filter(c -> c.id().equalsIgnoreCase(coingeckoCryptoId))
                    .findFirst()
                    .orElseThrow();
            var lastKnownPrice = crypto.lastKnownPrice();
            var lastKnownPriceInBTC = crypto.lastKnownPriceInBTC();
            var lastKnownPriceInEUR = crypto.lastKnownPriceInEUR();

            totalUSDBalance = totalUSDBalance.add(lastKnownPrice.multiply(quantity).setScale(2, RoundingMode.HALF_UP));
            totalBTCBalance = totalBTCBalance.add(lastKnownPriceInBTC.multiply(quantity)).stripTrailingZeros();
            totalEURBalance = totalEURBalance.add(lastKnownPriceInEUR.multiply(quantity).setScale(2, RoundingMode.HALF_UP));
        }

        return new BalancesResponse(
                totalUSDBalance.toPlainString(),
                totalEURBalance.toPlainString(),
                totalBTCBalance.setScale(12, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
        );
    }

    private BalancesResponse getCryptoTotalBalances(Crypto crypto, BigDecimal quantity) {
        return new BalancesResponse(
                crypto.lastKnownPrice().multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
                crypto.lastKnownPriceInEUR().multiply(quantity).setScale(2, RoundingMode.HALF_UP).toPlainString(),
                crypto.lastKnownPriceInBTC().multiply(quantity).setScale(12, RoundingMode.HALF_EVEN).stripTrailingZeros().toPlainString()
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
                    .filter(p -> p.id().equalsIgnoreCase(userCrypto.platformId()))
                    .findFirst()
                    .orElseThrow();

            if (platformsUserCryptos.containsKey(platform.name())) {
                var cryptos = new ArrayList<>(platformsUserCryptos.get(platform.name()));
                cryptos.add(userCrypto);
                platformsUserCryptos.put(platform.name(), cryptos);
            } else {
                platformsUserCryptos.put(platform.name(), List.of(userCrypto));
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
                    .filter(p -> p.id().equalsIgnoreCase(userCrypto.platformId()))
                    .findFirst()
                    .orElseThrow()
                    .name();

            if (map.containsKey(userCrypto.coingeckoCryptoId())) {
                var crypto = map.get(userCrypto.coingeckoCryptoId());
                var actualQuantity = crypto.component1();
                var actualPlatforms = new ArrayList<>(crypto.component2());

                var newQuantity = actualQuantity.add(userCrypto.quantity());
                actualPlatforms.add(platformName);

                map.put(userCrypto.coingeckoCryptoId(), new Pair<>(newQuantity, actualPlatforms));
            } else {
                map.put(userCrypto.coingeckoCryptoId(), new Pair<>(userCrypto.quantity(), List.of(platformName)));
            }
        });

        return map;
    }

    private boolean isLastPage(int page, int totalPages) {
        return page + 1 >= totalPages;
    }
}
