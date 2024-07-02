package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceTargetRepository extends JpaRepository<PriceTarget, String> {

    Optional<PriceTarget> findByCoingeckoCryptoIdAndTarget(String coingeckoCryptoId, BigDecimal target);
}
