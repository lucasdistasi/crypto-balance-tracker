package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.DateBalance;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface DateBalanceRepository extends MongoRepository<DateBalance, String> {

    List<DateBalance> findDateBalancesByDateBetween(LocalDateTime from, LocalDateTime to);
    List<DateBalance> findAllByDateIn(List<LocalDateTime> date);
    Optional<DateBalance> findDateBalanceByDate(LocalDateTime date);
}
