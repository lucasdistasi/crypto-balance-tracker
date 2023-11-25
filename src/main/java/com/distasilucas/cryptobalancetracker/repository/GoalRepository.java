package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface GoalRepository extends MongoRepository<Goal, String> {

    Optional<Goal> findByCoingeckoCryptoId(String coingeckoCryptoId);
}
