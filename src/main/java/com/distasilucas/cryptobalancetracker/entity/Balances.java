package com.distasilucas.cryptobalancetracker.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
public class Balances {
    @Column(name = "usd_balance")
    private String usdBalance;

    @Column(name = "eur_balance")
    private String eurBalance;

    @Column(name = "btc_balance")
    private String btcBalance;
}
