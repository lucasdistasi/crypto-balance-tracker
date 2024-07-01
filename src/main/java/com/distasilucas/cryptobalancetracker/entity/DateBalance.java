package com.distasilucas.cryptobalancetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "DateBalances")
public record DateBalance(

    @Id
    String id,
    LocalDate date,
    String balance
) {

    public DateBalance(LocalDate date, String balance) {
        this(UUID.randomUUID().toString(), date, balance);
    }
}
