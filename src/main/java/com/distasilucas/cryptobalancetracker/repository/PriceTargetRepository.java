package com.distasilucas.cryptobalancetracker.repository;

import com.distasilucas.cryptobalancetracker.entity.PriceTarget;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceTargetRepository extends MongoRepository<PriceTarget, String> {

    Optional<PriceTarget> findByCoingeckoCryptoIdAndTarget(String coingeckoCryptoId, BigDecimal target);
}
