package com.distasilucas.cryptobalancetracker.model.response.insights;

import com.distasilucas.cryptobalancetracker.entity.Crypto;
import com.distasilucas.cryptobalancetracker.entity.UserCrypto;

import java.util.List;

public record UserCryptosInsights(
    CryptoInfo cryptoInfo,
    String quantity,
    float percentage,
    BalancesResponse balances,
    int marketCapRank,
    MarketData marketData,
    List<String> platforms
) {

    public UserCryptosInsights(UserCrypto userCrypto, Crypto crypto, float percentage,
                               BalancesResponse balances, MarketData marketData, List<String> platforms) {
        this(
            new CryptoInfo(userCrypto, crypto),
            userCrypto.getQuantity().toPlainString(),
            percentage,
            balances,
            crypto.getCryptoInfo().getMarketCapRank(),
            marketData,
            platforms
        );
    }
}
