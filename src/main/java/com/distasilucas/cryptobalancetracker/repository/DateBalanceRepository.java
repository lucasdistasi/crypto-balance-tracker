package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DateBalanceRepository extends JpaRepository<DateBalance, String> {

    List<DateBalance> findDateBalancesByDateBetween(LocalDate from, LocalDate to);
    List<DateBalance> findAllByDateIn(List<LocalDate> date);
    Optional<DateBalance> findDateBalanceByDate(LocalDate date);
}
