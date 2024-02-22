package com.distasilucas.cryptobalancetracker.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@Document("DateBalances")
public record DateBalance(

    @Id
    String id,
    LocalDateTime date,
    String balance
) {

    public DateBalance(LocalDateTime date, String balance) {
        this(UUID.randomUUID().toString(), date, balance);
    }
}
