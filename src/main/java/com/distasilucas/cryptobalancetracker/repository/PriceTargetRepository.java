package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceTargetRepository extends JpaRepository<PriceTarget, String> {

    @Query(
        value = """
                SELECT price_targets
                FROM PriceTarget price_targets
                WHERE price_targets.crypto.id = :coingeckoCryptoId
                AND price_targets.target = :target
                """
    )
    Optional<PriceTarget> findByCoingeckoCryptoIdAndTarget(String coingeckoCryptoId, BigDecimal target);
}
