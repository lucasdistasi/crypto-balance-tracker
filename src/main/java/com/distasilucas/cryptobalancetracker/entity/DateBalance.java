package com.distasilucas.cryptobalancetracker.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "DateBalances")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DateBalance {

    @Id
    private String id;
    private LocalDate date;
    private String balance;

    public DateBalance(LocalDate date, String balance) {
        this.id = UUID.randomUUID().toString();
        this.date = date;
        this.balance = balance;
    }
}
