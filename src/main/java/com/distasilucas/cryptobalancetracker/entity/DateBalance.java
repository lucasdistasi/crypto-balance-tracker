package com.distasilucas.cryptobalancetracker.entity;

import com.distasilucas.cryptobalancetracker.model.response.insights.BalancesResponse;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "DateBalances")
@Getter
@NoArgsConstructor
public class DateBalance {

    @Id
    private String id;
    private LocalDate date;

    @Embedded
    private Balances balances;

    public DateBalance(LocalDate date, BalancesResponse balancesResponse) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.balances = new Balances(balancesResponse.totalUSDBalance(), balancesResponse.totalEURBalance(), balancesResponse.totalBTCBalance());
    }

    public DateBalance(String id, LocalDate date, BalancesResponse balancesResponse) {
        this.id = id;
        this.date = date;
        this.balances = new Balances(balancesResponse.totalUSDBalance(), balancesResponse.totalEURBalance(), balancesResponse.totalBTCBalance());
    }
}
