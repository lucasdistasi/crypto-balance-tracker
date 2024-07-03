package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.Goal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface GoalRepository extends JpaRepository<Goal, String> {

    @Query(
        value = """
                SELECT goals
                FROM Goal goals
                WHERE goals.crypto.id = :coingeckoCryptoId
                """
    )
    Optional<Goal> findByCoingeckoCryptoId(String coingeckoCryptoId);
}
