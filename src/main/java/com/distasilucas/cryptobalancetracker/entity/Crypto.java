package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.coingecko.CoingeckoCryptoInfo;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Getter
@Entity
@Table(name = "Cryptos")
@ToString
@NoArgsConstructor
public class Crypto implements Serializable {

    @Id
    private String id;

    @Embedded
    private CryptoInfo cryptoInfo;

    @Embedded
    private LastKnownPrices lastKnownPrices;

    @Embedded
    private ChangePercentages changePercentages;

    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @OneToMany(mappedBy = "crypto")
    private List<UserCrypto> userCryptos;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @OneToOne(mappedBy = "crypto")
    private Goal goal;

    @Getter(AccessLevel.NONE)
    @ToString.Exclude
    @OneToMany(mappedBy = "crypto")
    private List<PriceTarget> priceTargets;

    public Crypto(String id, CryptoInfo cryptoInfo, LastKnownPrices lastKnownPrices,
                  ChangePercentages changePercentages, LocalDateTime lastUpdatedAt) {
        this.id = id;
        this.cryptoInfo = cryptoInfo;
        this.lastKnownPrices = lastKnownPrices;
        this.changePercentages = changePercentages;
        this.lastUpdatedAt = lastUpdatedAt;
        this.userCryptos = Collections.emptyList();
        this.goal = null;
        this.priceTargets = Collections.emptyList();
    }

    public Crypto(CoingeckoCryptoInfo coingeckoCryptoInfo, LocalDateTime lastUpdatedAt) {
        this.id = coingeckoCryptoInfo.id();
        this.cryptoInfo = new CryptoInfo(coingeckoCryptoInfo);
        this.lastKnownPrices = new LastKnownPrices(coingeckoCryptoInfo.marketData());
        this.changePercentages = new ChangePercentages(coingeckoCryptoInfo.marketData());
        this.lastUpdatedAt = lastUpdatedAt;
        this.userCryptos = Collections.emptyList();
        this.goal = null;
        this.priceTargets = Collections.emptyList();
    }
}
